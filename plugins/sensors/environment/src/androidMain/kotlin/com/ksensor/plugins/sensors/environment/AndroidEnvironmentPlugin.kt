package com.ksensor.plugins.sensors.environment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ksensor.core.Permission
import com.ksensor.core.model.PluginId
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import kotlin.getValue

import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlin.math.log10
import android.media.MediaRecorder
import kotlin.time.Duration.Companion.milliseconds

class AndroidEnvironmentPlugin : EnvironmentPlugin {
    override val id: PluginId = PluginId.ENVIRONMENT
    override val requiredPermissions: List<Permission> = listOf(Permission.RECORD_AUDIO)

    private val sensorManager: SensorManager by lazy {
        KSensorContext.get().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val barometerFlows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Barometer>>>()
    private val lightFlows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.LightIlluminance>>>()
    private val proximityFlows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Proximity>>>()

    override fun barometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> =
        barometerFlows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
                if (sensor == null) {
                    close()
                    return@callbackFlow
                }
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        trySend(KSensorResponse(SensorData.Barometer(event.values[0])))
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensorManager.unregisterListener(listener) }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }

    override fun light(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> =
        lightFlows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                if (sensor == null) {
                    close()
                    return@callbackFlow
                }
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        trySend(KSensorResponse(SensorData.LightIlluminance(event.values[0])))
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensorManager.unregisterListener(listener) }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }

    override fun proximity(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> =
        proximityFlows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                if (sensor == null) {
                    close()
                    return@callbackFlow
                }
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val distance = event.values[0]
                        trySend(KSensorResponse(SensorData.Proximity(distance, distance < sensor.maximumRange)))
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensorManager.unregisterListener(listener) }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }

    private val noiseFlows = ConcurrentHashMap<Pair<SensorConfig, Float>, Flow<KSensorResponse<SensorData.Noise>>>()

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
                    // Start might fail if microphone is not available or busy
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

actual fun createEnvironmentPlugin(): EnvironmentPlugin = AndroidEnvironmentPlugin()
