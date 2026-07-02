package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

class EngineSoundGenerator {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var rpm: Float = 1000f // Idle RPM is 1000, max is ~8000
    @Volatile
    private var bikeId: String = "splendor"
    @Volatile
    private var exhaustStage: Int = 0

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize * 2,
                    AudioTrack.MODE_STREAM
                )
            }
        } catch (e: Exception) {
            Log.e("EngineSound", "Failed to init AudioTrack: ${e.message}")
        }
    }

    fun setBikeType(id: String) {
        this.bikeId = id
    }

    fun setRpm(rpmValue: Float) {
        this.rpm = rpmValue.coerceIn(800f, 10000f)
    }

    fun setExhaustStage(stage: Int) {
        this.exhaustStage = stage
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true
        audioTrack?.play()

        job = scope.launch {
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            var phase = 0.0

            while (isActive && isPlaying) {
                // Calculate target frequency based on RPM and bike type
                // Bullet has a low frequency thump, KTM has a buzzing screaming frequency
                val baseFreq = when (bikeId) {
                    "bullet" -> (rpm / 60f) * 0.5f // Low thump, sub-harmonics
                    "ktm" -> (rpm / 60f) * 2.2f     // Screamer, higher frequency
                    "pulsar" -> (rpm / 60f) * 1.5f  // Mid-range raspy dual-pulse
                    else -> (rpm / 60f) * 1.0f      // Splendor - smooth single pulse
                }

                // Volume multiplier increases with RPM and upgraded exhaust
                val volumeMultiplier = 0.4f + (exhaustStage * 0.18f) + ((rpm - 1000f) / 9000f) * 0.4f
                val finalVol = volumeMultiplier.coerceIn(0.1f, 1.0f)

                for (i in 0 until bufferSize) {
                    val angle = 2.0 * Math.PI * baseFreq / sampleRate
                    phase += angle
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI
                    }

                    // Generate waves based on bike characteristics
                    val sampleValue = when (bikeId) {
                        "bullet" -> {
                            // Heavy square-like double-pulse with deep thump
                            val wave = sin(phase) + 0.5 * sin(2.0 * phase) + 0.25 * sin(3.0 * phase)
                            // Simulate engine misfires/gap thumps at low RPM
                            val gate = if (sin(phase * 0.5) > -0.2) 1.0 else 0.08
                            (wave * gate * 25000.0 * finalVol).toInt().toShort()
                        }
                        "ktm" -> {
                            // Sawtooth screamer
                            val t = phase / (2.0 * Math.PI)
                            val wave = 2.0 * t - 1.0 // raw sawtooth
                            val smoothWave = wave + 0.3 * sin(3.0 * phase)
                            (smoothWave * 18000.0 * finalVol).toInt().toShort()
                        }
                        "pulsar" -> {
                            // Aggressive triangle hybrid wave
                            val t = phase / (2.0 * Math.PI)
                            val tri = if (t < 0.5) 4.0 * t - 1.0 else 3.0 - 4.0 * t
                            val comb = tri + 0.4 * sin(4.0 * phase)
                            (comb * 20000.0 * finalVol).toInt().toShort()
                        }
                        else -> {
                            // Splendor: soft, clean drone
                            val wave = sin(phase) + 0.2 * sin(2.0 * phase)
                            (wave * 15000.0 * finalVol).toInt().toShort()
                        }
                    }
                    buffer[i] = sampleValue
                }

                audioTrack?.write(buffer, 0, bufferSize)
            }
        }
    }

    fun stop() {
        isPlaying = false
        job?.cancel()
        job = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        stop()
        try {
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }
}
