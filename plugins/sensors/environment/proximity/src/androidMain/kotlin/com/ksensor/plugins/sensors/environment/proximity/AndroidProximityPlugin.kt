package com.ksensor.plugins.sensors.environment.proximity

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidProximityPlugin : ProximityPlugin {
    private val sensorManager: SensorManager by lazy {
        KSensorContext.get().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Proximity>>>()

    @SuppressLint("NewApi")
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> =
        flows.getOrPut(config) {
            callbackFlow {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                if (sensor == null) {
                    KSensor.reportStatus(
                        pluginId = id,
                        status = PluginStatus.UNSUPPORTED,
                        unsupportedReason = "Proximity sensor hardware is not available on this device"
                    )
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
            }
                .trackPluginDiagnostics(id)
                .shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createProximityPlugin(): ProximityPlugin = AndroidProximityPlugin()
