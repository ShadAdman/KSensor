package com.ksensor.plugins.sensors.environment.noise

import android.media.MediaRecorder
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log10
import kotlin.time.Duration.Companion.milliseconds

class AndroidNoisePlugin : NoisePlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val noiseFlows = ConcurrentHashMap<Pair<SensorConfig, Float>, Flow<KSensorResponse<SensorData.Noise>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Noise>> = noise(config, 80f)

    override fun noise(config: SensorConfig, loudThresholdDb: Float): Flow<KSensorResponse<SensorData.Noise>> =
        noiseFlows.getOrPut(Pair(config, loudThresholdDb)) {
            callbackFlow {
                var recorder: MediaRecorder? = null
                var isRecording = false
                try {
                    @Suppress("DEPRECATION")
                    recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        MediaRecorder(KSensorContext.get())
                    } else {
                        MediaRecorder()
                    }

                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    val tempFile = java.io.File(KSensorContext.get().cacheDir, "noise_temp.3gp")
                    recorder.setOutputFile(tempFile.absolutePath)

                    recorder.prepare()
                    recorder.start()
                    isRecording = true

                    while (isActive && isRecording) {
                        val amplitude = recorder.maxAmplitude
                        val db = if (amplitude > 0) {
                            20 * log10(amplitude.toDouble()).toFloat()
                        } else {
                            0f
                        }

                        trySend(
                            KSensorResponse(
                                data = SensorData.Noise(
                                    dB = db,
                                    isLoud = db > loudThresholdDb,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        )

                        delay(config.intervalMs.milliseconds)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        if (isRecording) {
                            recorder?.stop()
                        }
                    } catch (e: Exception) {
                        // Ignore exception on stop
                    }
                    recorder?.release()
                }
                awaitClose {
                    isRecording = false
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createNoisePlugin(): NoisePlugin = AndroidNoisePlugin()
