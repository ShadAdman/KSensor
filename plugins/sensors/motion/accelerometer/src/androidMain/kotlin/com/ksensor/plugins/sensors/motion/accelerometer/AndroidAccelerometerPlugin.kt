package com.ksensor.plugins.sensors.motion.accelerometer

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

class AndroidAccelerometerPlugin : AccelerometerPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Accelerometer>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Accelerometer>> =
        flows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                if (sensor == null) {
                    close()
                    return@callbackFlow
                }

                val maximumRange = sensor.maximumRange
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val data = SensorData.Accelerometer(
                            Vector3(
                                event.values[0] / maximumRange,
                                event.values[1] / maximumRange,
                                event.values[2] / maximumRange
                            )
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

actual fun createAccelerometerPlugin(): AccelerometerPlugin = AndroidAccelerometerPlugin()
