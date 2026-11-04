package com.ksensor.plugins.sensors.motion.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidGyroscopePlugin : GyroscopePlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Gyroscope>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Gyroscope>> =
        flows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                if (sensor == null) {
                    close()
                    return@callbackFlow
                }

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val data = SensorData.Gyroscope(
                            Vector3(event.values[0], event.values[1], event.values[2])
                        )
                        trySend(KSensorResponse(data))
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensorManager.unregisterListener(listener) }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createGyroscopePlugin(): GyroscopePlugin = AndroidGyroscopePlugin()
