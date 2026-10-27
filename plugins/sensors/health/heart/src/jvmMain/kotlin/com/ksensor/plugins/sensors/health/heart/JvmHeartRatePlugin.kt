package com.ksensor.plugins.sensors.health.heart

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmHeartRatePlugin : HeartRatePlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.HeartRate>> = emptyFlow()
}

actual fun createHeartRatePlugin(): HeartRatePlugin = JvmHeartRatePlugin()
