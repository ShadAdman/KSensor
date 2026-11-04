package com.ksensor.plugins.sensors.health.heart

import android.content.Context
import android.util.Log
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidHeartRatePlugin : HeartRatePlugin {
    private val context: Context by lazy { KSensorContext.get() }

    private val sensorManagerMonitor by lazy { SensorManagerHeartRateMonitor(context) }
    private val cameraMonitor by lazy { CameraHeartRateMonitor(context) }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val heartRateFlows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.HeartRate>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.HeartRate>> =
        heartRateFlows.getOrPut(config) {
            flow {
                if (sensorManagerMonitor.isSupported()) {
                    Log.i("AndroidHeartRatePlugin", "Using Hardware Sensor for heart rate")
                    emitAll(sensorManagerMonitor.start())
                } else if (cameraMonitor.isSupported()) {
                    Log.i("AndroidHeartRatePlugin", "Falling back to Camera PPG for heart rate")
                    emit(KSensorResponse(SensorData.HeartRate(0f, SensorData.HeartRateSource.CAMERA_PPG)))
                    emitAll(cameraMonitor.start())
                } else {
                    Log.w("AndroidHeartRatePlugin", "Heart rate monitoring is not supported on this device")
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createHeartRatePlugin(): HeartRatePlugin = AndroidHeartRatePlugin()
