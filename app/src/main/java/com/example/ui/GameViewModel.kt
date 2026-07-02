package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.EngineSoundGenerator
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

enum class GameScreen {
    MENU, GARAGE, IGNITION, RACE, FINISH
}

enum class Weather {
    SUNNY, RAINY, FOGGY, NIGHT
}

data class TrackInfo(
    val id: String,
    val name: String,
    val distance: Float, // in meters
    val difficulty: String,
    val reward: Int,
    val description: String,
    val baseWeather: Weather
)

data class VehicleState(
    val model: String, // "Thar", "Swift", "PMPML Bus", "Auto", "Tata Truck"
    val name: String,
    var distance: Float, // relative to track start
    var lane: Int, // -1 (left), 0 (center), 1 (right)
    val speed: Float, // constant speed
    val width: Float = 0.6f
)

data class RivalState(
    val name: String,
    val bikeModel: String,
    var distance: Float,
    var laneX: Float, // -1.0 to 1.0
    var health: Float = 100f,
    var isKnockedOut: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())
    private val soundGenerator = EngineSoundGenerator()

    // Database flows
    val allBikes: StateFlow<List<BikeEntity>> = repository.allBikes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBike: StateFlow<BikeEntity?> = repository.activeBike
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(GameScreen.MENU)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Selected track for race
    private val _selectedTrack = MutableStateFlow<TrackInfo?>(null)
    val selectedTrack: StateFlow<TrackInfo?> = _selectedTrack.asStateFlow()

    // Selected weather (can override baseWeather)
    private val _selectedWeather = MutableStateFlow(Weather.SUNNY)
    val selectedWeather: StateFlow<Weather> = _selectedWeather.asStateFlow()

    // Battery Saver Mode
    private val _batterySaverMode = MutableStateFlow(false)
    val batterySaverMode: StateFlow<Boolean> = _batterySaverMode.asStateFlow()

    // IGNITION / COCKPIT Checklist State
    private val _isKeyInserted = MutableStateFlow(false)
    val isKeyInserted: StateFlow<Boolean> = _isKeyInserted.asStateFlow()

    private val _isIgnitionOn = MutableStateFlow(false)
    val isIgnitionOn: StateFlow<Boolean> = _isIgnitionOn.asStateFlow()

    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning: StateFlow<Boolean> = _isEngineRunning.asStateFlow()

    private val _isStarterPressed = MutableStateFlow(false)
    val isStarterPressed: StateFlow<Boolean> = _isStarterPressed.asStateFlow()

    private val _ignitionCockpitStatus = MutableStateFlow("गाडी सुरु करा! चावी लावा आणि इग्निशन ऑन करा.")
    val ignitionCockpitStatus: StateFlow<String> = _ignitionCockpitStatus.asStateFlow()

    // ACTIVE RACE variables
    private val _playerSpeed = MutableStateFlow(0f) // km/h
    val playerSpeed: StateFlow<Float> = _playerSpeed.asStateFlow()

    private val _playerRpm = MutableStateFlow(1000f)
    val playerRpm: StateFlow<Float> = _playerRpm.asStateFlow()

    private val _playerGear = MutableStateFlow(1)
    val playerGear: StateFlow<Int> = _playerGear.asStateFlow()

    private val _playerX = MutableStateFlow(0f) // -1f (left edge) to 1f (right edge)
    val playerX: StateFlow<Float> = _playerX.asStateFlow()

    private val _distanceTravelled = MutableStateFlow(0f) // in meters
    val distanceTravelled: StateFlow<Float> = _distanceTravelled.asStateFlow()

    private val _playerHealth = MutableStateFlow(100f)
    val playerHealth: StateFlow<Float> = _playerHealth.asStateFlow()

    private val _combatMessage = MutableStateFlow("")
    val combatMessage: StateFlow<String> = _combatMessage.asStateFlow()

    val traffic = mutableStateListOf<VehicleState>()
    val rivals = mutableStateListOf<RivalState>()

    // Post-race summary results
    private val _raceFinishStatus = MutableStateFlow(false) // true: Won, false: Busted/Wrecked
    val raceFinishStatus: StateFlow<Boolean> = _raceFinishStatus.asStateFlow()

    private val _raceEarning = MutableStateFlow(0)
    val raceEarning: StateFlow<Int> = _raceEarning.asStateFlow()

    private val _raceTimeElapsed = MutableStateFlow(0) // seconds
    val raceTimeElapsed: StateFlow<Int> = _raceTimeElapsed.asStateFlow()

    // Available Tracks in Pune and Pimpri Chinchwad
    val tracks = listOf(
        TrackInfo(
            id = "swargate",
            name = "शनिवार वाडा ते स्वारगेट (Shaniwar Wada to Swargate)",
            distance = 4000f,
            difficulty = "मध्यम (Medium)",
            reward = 4000,
            description = "शनिवार वाड्याच्या ऐतिहासिक दरवाजापासून दगडूशेठ मंदिर मार्गे स्वारगेट चौकापर्यंत! तुफान रहदारी (PMPML बसेस, रिक्षा) आणि तीव्र वळणे.",
            baseWeather = Weather.SUNNY
        ),
        TrackInfo(
            id = "fc_road",
            name = "फर्ग्युसन कॉलेज रोड (FC Road Lights)",
            distance = 3200f,
            difficulty = "सोपे (Easy)",
            reward = 2500,
            description = "तरुणांची आवडती सुंदर पुण्याची रात्र! रस्त्यावरील निऑन लाईट्स आणि रोषणाईमधून वेगवान फेरफटका.",
            baseWeather = Weather.NIGHT
        ),
        TrackInfo(
            id = "sinhagad",
            name = "सिंहगड घाट रोड (Sinhagad Ghat Climb)",
            distance = 6000f,
            difficulty = "कठीण (Hard)",
            reward = 7500,
            description = "मुसळधार पाऊस, दाट धुके आणि जीवघेणी धोकादायक घाटाची वळणे! सिंहगड किल्ला सर करण्यासाठी सर्वोच्च नियंत्रणाची गरज.",
            baseWeather = Weather.RAINY
        ),
        TrackInfo(
            id = "pcmc_highway",
            name = "पिंपरी-चिंचवड एक्सप्रेसवे (Nigdi to Wakad)",
            distance = 8000f,
            difficulty = "अति-कठीण (Pro)",
            reward = 12000,
            description = "PCMC मधील रुंद एक्सप्रेस हायवे! अजस्त्र टाटा ट्रक्स, वेगवान सुसाट कार्स आणि अतिवेगवान रायडिंगचा थरार.",
            baseWeather = Weather.FOGGY
        )
    )

    private var gameLoopJob: Job? = null
    private var raceStartTime = 0L

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    fun setScreen(screen: GameScreen) {
        _currentScreen.value = screen
        if (screen == GameScreen.MENU) {
            soundGenerator.stop()
            resetIgnitionState()
        }
    }

    fun selectTrack(track: TrackInfo) {
        _selectedTrack.value = track
        _selectedWeather.value = track.baseWeather
    }

    fun overrideWeather(weather: Weather) {
        _selectedWeather.value = weather
    }

    fun toggleBatterySaver() {
        _batterySaverMode.value = !_batterySaverMode.value
    }

    fun setRpm(rpmValue: Float) {
        _playerRpm.value = rpmValue
        soundGenerator.setRpm(rpmValue)
    }

    // IGNITION INTERACTION METHODS
    fun insertKey() {
        _isKeyInserted.value = true
        _ignitionCockpitStatus.value = "चावी लावली आहे! आता इग्निशन ऑन करण्यासाठी चावी फिरवा."
        soundGenerator.setBikeType(activeBike.value?.id ?: "splendor")
        soundGenerator.setExhaustStage(activeBike.value?.exhaustStage ?: 0)
    }

    fun turnIgnitionOn() {
        if (!_isKeyInserted.value) return
        _isIgnitionOn.value = true
        _ignitionCockpitStatus.value = "डॅशबोर्ड सुरू झाला! आरपीएम सुईचे चेकिंग पूर्ण झाले. आता सेल्फ-स्टार्ट बटन दाबून ठेवा."
    }

    fun pressStarter(pressed: Boolean) {
        if (!_isIgnitionOn.value) return
        _isStarterPressed.value = pressed
        if (pressed && !_isEngineRunning.value) {
            _ignitionCockpitStatus.value = "इंजिन स्टार्ट होत आहे... कुर्रर्र..."
            viewModelScope.launch {
                delay(1200)
                if (_isStarterPressed.value) {
                    _isEngineRunning.value = true
                    _ignitionCockpitStatus.value = "इंजिन जिवंत झाले! डुक-डुक-डुक... आता रेस सुरू करायला तुम्ही तयार आहात!"
                    soundGenerator.start()
                    soundGenerator.setRpm(1200f) // Idle RPM
                }
            }
        }
    }

    private fun resetIgnitionState() {
        _isKeyInserted.value = false
        _isIgnitionOn.value = false
        _isEngineRunning.value = false
        _isStarterPressed.value = false
        _ignitionCockpitStatus.value = "गाडी सुरू करा! चावी लावा आणि इग्निशन ऑन करा."
        _playerSpeed.value = 0f
        _playerRpm.value = 1000f
        _playerGear.value = 1
        _distanceTravelled.value = 0f
        _playerHealth.value = 100f
        _combatMessage.value = ""
    }

    // RACING CORE LOOP
    fun startRace() {
        val bike = activeBike.value ?: return
        val track = _selectedTrack.value ?: return

        resetIgnitionState()
        _isKeyInserted.value = true
        _isIgnitionOn.value = true
        _isEngineRunning.value = true

        soundGenerator.setBikeType(bike.id)
        soundGenerator.setExhaustStage(bike.exhaustStage)
        soundGenerator.start()
        soundGenerator.setRpm(1200f)

        _currentScreen.value = GameScreen.RACE
        raceStartTime = System.currentTimeMillis()

        // Populate traffic & rivals based on track info
        traffic.clear()
        rivals.clear()

        // Add 5 rivals
        val names = listOf("राहुल पिंपरीकर", "रोहन धनकवडी", "अनिकेत कोथरूडकर", "सौरभ वाकडकर", "शुभम सिंहगडकर")
        val bikeModels = listOf("splendor", "pulsar", "bullet", "ktm", "pulsar")
        for (i in names.indices) {
            rivals.add(
                RivalState(
                    name = names[i],
                    bikeModel = bikeModels[i],
                    distance = 150f + i * 200f,
                    laneX = if (i % 2 == 0) -0.5f else 0.5f
                )
            )
        }

        // Add traffic
        val trafficModels = listOf("PMPML Bus", "Thar", "Auto", "Swift", "Tata Truck")
        for (i in 0..15) {
            traffic.add(
                VehicleState(
                    model = trafficModels[i % trafficModels.size],
                    name = when (trafficModels[i % trafficModels.size]) {
                        "PMPML Bus" -> "PMPML बस (लाल डबा)"
                        "Thar" -> "महिंद्रा थार"
                        "Auto" -> "पुणेरी रिक्षा"
                        "Swift" -> "स्विफ्ट कार"
                        else -> "टाटा ट्रक"
                    },
                    distance = 300f + i * 280f,
                    lane = (i % 3) - 1, // -1, 0, 1
                    speed = 30f + Random.nextInt(20) // km/h
                )
            )
        }

        // Start thread-safe game loop
        gameLoopJob?.cancel()
        val delayMs = if (_batterySaverMode.value) 33L else 16L // ~30 FPS vs ~60 FPS (battery optimization!)
        gameLoopJob = viewModelScope.launch {
            while (isActive && _currentScreen.value == GameScreen.RACE) {
                updateGameTick(delayMs / 1000f)
                delay(delayMs)
            }
        }
    }

    private fun updateGameTick(dt: Float) {
        val bike = activeBike.value ?: return
        val track = _selectedTrack.value ?: return

        // 1. Calculate player max values based on upgrade stages
        // Upgrades boost stats:
        val engineBonus = 1f + (bike.engineStage * 0.12f)
        val airFilterBonus = 1f + (bike.airFilterStage * 0.08f)
        val ecuBonus = 1f + (bike.ecuStage * 0.10f)

        val maxSpeed = (bike.baseSpeed + 20f) * engineBonus * airFilterBonus
        val maxAcceleration = (bike.baseAcceleration + 15f) * engineBonus * ecuBonus
        val maxHandling = (bike.baseHandling + 10f) * (1f + (bike.paintColor * 0.01f)) // just minor aesthetic grip representation

        // 2. Drive physics: Apply gradual acceleration based on RPM & gear
        val speed = _playerSpeed.value
        val gear = _playerGear.value

        // Simple gear progression
        val targetMaxForGear = maxSpeed * (gear / 5f)
        val currentAcc = maxAcceleration * 0.4f * (1.1f - (speed / targetMaxForGear).coerceIn(0f, 1f))

        var newSpeed = speed
        if (speed < targetMaxForGear) {
            newSpeed += currentAcc * dt
        } else if (speed > targetMaxForGear + 5f) {
            newSpeed -= 15f * dt // natural engine brake
        }

        // Handle Weather effects on physics (rain slippery, fog visibility)
        val weatherFactor = when (_selectedWeather.value) {
            Weather.RAINY -> 0.8f // reduced tire grip, slightly slower
            Weather.FOGGY -> 0.9f
            else -> 1.0f
        }
        newSpeed *= weatherFactor

        _playerSpeed.value = newSpeed.coerceIn(0f, maxSpeed)

        // Calculate responsive RPM based on current gear and speed ratio
        val speedRatioInGear = (speed % (maxSpeed / 5f)) / (maxSpeed / 5f)
        val targetRpm = 1000f + (speedRatioInGear * 7000f) + (if (speed > 5) 500f else 0f)
        _playerRpm.value = targetRpm.coerceIn(1000f, 9000f)

        // Pass dynamic RPM to audio engine
        soundGenerator.setRpm(_playerRpm.value)

        // 3. Move player forward
        val speedInMetersPerSecond = (newSpeed * 1000f) / 3600f
        val currentDist = _distanceTravelled.value + (speedInMetersPerSecond * dt)
        _distanceTravelled.value = currentDist

        // 4. Update traffic positions relative to player
        for (v in traffic) {
            // vehicles drive forward slowly too
            val vSpeedMps = (v.speed * 1000f) / 3600f
            v.distance += vSpeedMps * dt
        }

        // 5. Update rivals positions
        for (r in rivals) {
            if (!r.isKnockedOut) {
                // Rivals speed depends on their models
                val rivalBase = when (r.bikeModel) {
                    "ktm" -> 110f
                    "bullet" -> 90f
                    "pulsar" -> 100f
                    else -> 75f
                }
                val rSpeedMps = (rivalBase * 1000f) / 3600f
                r.distance += rSpeedMps * dt

                // Keep rivals within a reasonable range to keep fight intense
                if (r.distance < currentDist - 100f) {
                    r.distance = currentDist - 80f // pull them up
                } else if (r.distance > currentDist + 150f) {
                    r.distance = currentDist + 130f // drop them back
                }
            }
        }

        // 6. Check for collisions (with traffic vehicles)
        val playerXPos = _playerX.value
        for (v in traffic) {
            // if player passes the vehicle
            val distDiff = Math.abs(v.distance - currentDist)
            if (distDiff < 8f) {
                // Convert lane to X bounds (-0.7 for lane -1, 0.0 for lane 0, 0.7 for lane 1)
                val laneX = when (v.lane) {
                    -1 -> -0.6f
                    1 -> 0.6f
                    else -> 0.0f
                }
                if (Math.abs(laneX - playerXPos) < 0.35f) {
                    // CRASH!
                    _playerHealth.value = (_playerHealth.value - 30f).coerceIn(0f, 100f)
                    _playerSpeed.value = 10f // slow down completely
                    _combatMessage.value = "धडक झाली! ${v.name} ने धडक दिली!"
                    clearCombatMessageAfterDelay()
                }
            }
        }

        // 7. Check Win / Loss condition
        if (_playerHealth.value <= 0f) {
            // Wrecked / Busted
            endRace(won = false)
        } else if (currentDist >= track.distance) {
            // Finished!
            endRace(won = true)
        }
    }

    private fun clearCombatMessageAfterDelay() {
        viewModelScope.launch {
            delay(2000)
            _combatMessage.value = ""
        }
    }

    // Kicking / Punching opponents (Classic Road Rash mechanics!)
    fun performCombatAction(type: String) {
        val currentDist = _distanceTravelled.value
        val playerXPos = _playerX.value

        // Find closest rival within striking range (distance < 12m and X side match)
        val nearbyRival = rivals.firstOrNull { r ->
            !r.isKnockedOut && Math.abs(r.distance - currentDist) < 15f && Math.abs(r.laneX - playerXPos) < 0.6f
        }

        if (nearbyRival != null) {
            val damage = when (type) {
                "kick" -> 25f
                "punch" -> 15f
                "chain" -> 45f // high unlock damage
                else -> 10f
            }
            nearbyRival.health -= damage
            if (nearbyRival.health <= 0f) {
                nearbyRival.isKnockedOut = true
                nearbyRival.distance -= 40f // drop back far
                _combatMessage.value = "जबरदस्त राडा! ${nearbyRival.name} ला खाली पाडले!"
                // Reward player with instant cash boost for knocking out local gunda
                viewModelScope.launch {
                    repository.addCash(500)
                }
            } else {
                _combatMessage.value = when (type) {
                    "kick" -> "लाथ मारली! ${nearbyRival.name} चे संतुलन बिघडले."
                    else -> "जोराचा ठोसा! ${nearbyRival.name} घायाळ."
                }
            }
            clearCombatMessageAfterDelay()
        } else {
            _combatMessage.value = "हवेत ${if (type == "kick") "लाथ" else "ठोसा"} मारली! जवळ कोणी नाही."
            clearCombatMessageAfterDelay()
        }
    }

    fun steerLeft() {
        _playerX.value = (_playerX.value - 0.08f).coerceIn(-1.0f, 1.0f)
    }

    fun steerRight() {
        _playerX.value = (_playerX.value + 0.08f).coerceIn(-1.0f, 1.0f)
    }

    fun shiftGearUp() {
        if (_playerGear.value < 5) {
            _playerGear.value += 1
            // temporary drop in RPM on gear shift
            _playerRpm.value = (_playerRpm.value * 0.7f).coerceAtLeast(1200f)
            soundGenerator.setRpm(_playerRpm.value)
        }
    }

    fun shiftGearDown() {
        if (_playerGear.value > 1) {
            _playerGear.value -= 1
            _playerRpm.value = (_playerRpm.value * 1.3f).coerceAtMost(9000f)
            soundGenerator.setRpm(_playerRpm.value)
        }
    }

    private fun endRace(won: Boolean) {
        gameLoopJob?.cancel()
        soundGenerator.stop()

        _raceFinishStatus.value = won
        val track = _selectedTrack.value ?: return
        val elapsedSec = ((System.currentTimeMillis() - raceStartTime) / 1000).toInt()
        _raceTimeElapsed.value = elapsedSec

        if (won) {
            _raceEarning.value = track.reward
            viewModelScope.launch {
                repository.addCash(track.reward)
                // Unlock next tracks sequentially
                when (track.id) {
                    "swargate" -> repository.unlockTrack("fc_road")
                    "fc_road" -> repository.unlockTrack("sinhagad")
                    "sinhagad" -> repository.unlockTrack("pcmc_highway")
                }
            }
        } else {
            _raceEarning.value = 0
        }

        _currentScreen.value = GameScreen.FINISH
    }

    // GARAGE / MODIFICATION SYSTEM
    fun buyBike(bike: BikeEntity) {
        viewModelScope.launch {
            val success = repository.buyBike(bike.id, bike.price)
            if (success) {
                repository.selectBike(bike.id)
            }
        }
    }

    fun selectBike(bikeId: String) {
        viewModelScope.launch {
            repository.selectBike(bikeId)
        }
    }

    fun buyUpgrade(bikeId: String, type: String, cost: Int) {
        viewModelScope.launch {
            repository.upgradeComponent(bikeId, type, cost)
        }
    }

    fun saveCustomizations(bikeId: String, paintIndex: Int, neonIndex: Int, sticker: String) {
        viewModelScope.launch {
            repository.saveAesthetics(bikeId, paintIndex, neonIndex, sticker)
        }
    }

    fun cheatAddCash() {
        viewModelScope.launch {
            repository.addCash(5000)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundGenerator.release()
    }
}
