package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val allBikes: Flow<List<BikeEntity>> = gameDao.getAllBikes()
    val activeBike: Flow<BikeEntity?> = gameDao.getActiveBike()
    val playerProfile: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()

    suspend fun checkAndPrepopulate() {
        val currentBikes = gameDao.getAllBikes().firstOrNull()
        if (currentBikes.isNullOrEmpty()) {
            val defaultBikes = listOf(
                BikeEntity(
                    id = "splendor",
                    name = "काळजाचा तुकडा (Splendor)",
                    englishName = "Splendor",
                    description = "पुण्यातील गल्लीबोळातून सहज निघणारी, सुटसुटीत आणि सगळ्यात जास्त मायलेज देणारी विश्वासू गाडी!",
                    owned = true,
                    current = true,
                    baseSpeed = 65f,
                    baseAcceleration = 60f,
                    baseHandling = 95f,
                    price = 0
                ),
                BikeEntity(
                    id = "pulsar",
                    name = "राडा किंग (Pulsar 220)",
                    englishName = "Pulsar 220",
                    description = "तरुणांची आवडती, झक्कास पिकअप आणि वेगवान थरार! पुणे-पिंपरी रस्त्यांवर राडा करायला एकदम परफेक्ट.",
                    owned = false,
                    current = false,
                    baseSpeed = 82f,
                    baseAcceleration = 78f,
                    baseHandling = 80f,
                    price = 8000
                ),
                BikeEntity(
                    id = "bullet",
                    name = "रॉयल बुलेट ३५० (Royal Bullet)",
                    englishName = "Bullet 350",
                    description = "पुणेकरांची शान! डुक-डुक आवाजाचा भारी डौल. सिंहगड घाटात चढताना ताकद आणि ऐट दोन्ही दाखवते.",
                    owned = false,
                    current = false,
                    baseSpeed = 75f,
                    baseAcceleration = 88f,
                    baseHandling = 72f,
                    price = 16000
                ),
                BikeEntity(
                    id = "ktm",
                    name = "पॉकेट रॉकेट (KTM RC)",
                    englishName = "KTM RC",
                    description = "पिंपरी चिंचवडच्या हायवेवर सुसाट पळणारी वेगवान रेसर! वाऱ्याच्या वेगाने धावणारी आणि कमालीचे नियंत्रण देणारी गाडी.",
                    owned = false,
                    current = false,
                    baseSpeed = 95f,
                    baseAcceleration = 92f,
                    baseHandling = 88f,
                    price = 28000
                )
            )
            gameDao.insertBikes(defaultBikes)
        }

        val profile = gameDao.getPlayerProfile().firstOrNull()
        if (profile == null) {
            gameDao.insertPlayerProfile(PlayerProfileEntity(id = 1, cash = 5000, unlockedTracks = "swargate,fc_road"))
        }
    }

    suspend fun selectBike(bikeId: String) {
        val bikes = gameDao.getAllBikes().firstOrNull() ?: return
        for (bike in bikes) {
            if (bike.id == bikeId && bike.owned) {
                gameDao.updateBike(bike.copy(current = true))
            } else if (bike.current) {
                gameDao.updateBike(bike.copy(current = false))
            }
        }
    }

    suspend fun buyBike(bikeId: String, price: Int): Boolean {
        val profile = gameDao.getPlayerProfile().firstOrNull() ?: return false
        val bike = gameDao.getBikeById(bikeId) ?: return false

        if (!bike.owned && profile.cash >= price) {
            // Deduct cash and mark owned
            gameDao.updatePlayerProfile(profile.copy(cash = profile.cash - price))
            gameDao.updateBike(bike.copy(owned = true))
            return true
        }
        return false
    }

    suspend fun upgradeComponent(bikeId: String, type: String, cost: Int): Boolean {
        val profile = gameDao.getPlayerProfile().firstOrNull() ?: return false
        val bike = gameDao.getBikeById(bikeId) ?: return false

        if (profile.cash >= cost) {
            val updatedBike = when (type) {
                "engine" -> if (bike.engineStage < 3) bike.copy(engineStage = bike.engineStage + 1) else null
                "ecu" -> if (bike.ecuStage < 3) bike.copy(ecuStage = bike.ecuStage + 1) else null
                "exhaust" -> if (bike.exhaustStage < 3) bike.copy(exhaustStage = bike.exhaustStage + 1) else null
                "airFilter" -> if (bike.airFilterStage < 3) bike.copy(airFilterStage = bike.airFilterStage + 1) else null
                else -> null
            } ?: return false

            gameDao.updatePlayerProfile(profile.copy(cash = profile.cash - cost))
            gameDao.updateBike(updatedBike)
            return true
        }
        return false
    }

    suspend fun saveAesthetics(bikeId: String, paintIndex: Int, neonIndex: Int, sticker: String): Boolean {
        val bike = gameDao.getBikeById(bikeId) ?: return false
        gameDao.updateBike(bike.copy(
            paintColor = paintIndex,
            neonColor = neonIndex,
            stickerText = sticker
        ))
        return true
    }

    suspend fun addCash(amount: Int) {
        val profile = gameDao.getPlayerProfile().firstOrNull() ?: return
        gameDao.updatePlayerProfile(profile.copy(cash = profile.cash + amount))
    }

    suspend fun unlockTrack(trackId: String) {
        val profile = gameDao.getPlayerProfile().firstOrNull() ?: return
        val currentTracks = profile.unlockedTracks.split(",").toMutableSet()
        if (!currentTracks.contains(trackId)) {
            currentTracks.add(trackId)
            gameDao.updatePlayerProfile(profile.copy(unlockedTracks = currentTracks.joinToString(",")))
        }
    }
}
