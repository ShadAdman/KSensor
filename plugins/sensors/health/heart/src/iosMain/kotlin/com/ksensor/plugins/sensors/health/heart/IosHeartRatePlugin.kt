package com.ksensor.plugins.sensors.health.heart

import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class IosHeartRatePlugin : HeartRatePlugin {
    private val healthKitMonitor by lazy { HealthKitHeartRateMonitor() }
    private val cameraMonitor by lazy { IosCameraHeartRateMonitor() }

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.HeartRate>> = flow {
        if (healthKitMonitor.isSupported()) {
            emitAll(healthKitMonitor.start())
        } else if (cameraMonitor.isSupported()) {
            emit(KSensorResponse(SensorData.HeartRate(0f, SensorData.HeartRateSource.CAMERA_PPG)))
            emitAll(cameraMonitor.start())
        } else {
            KSensor.reportStatus(
                pluginId = id,
                status = PluginStatus.UNSUPPORTED,
                unsupportedReason = "Neither HealthKit heart rate nor camera PPG is available on this iOS device"
            )
        }
    }.trackPluginDiagnostics(id)
}

actual fun createHeartRatePlugin(): HeartRatePlugin = IosHeartRatePlugin()
