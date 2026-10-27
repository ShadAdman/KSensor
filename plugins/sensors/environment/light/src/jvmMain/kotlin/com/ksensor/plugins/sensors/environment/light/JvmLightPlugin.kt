package com.ksensor.plugins.sensors.environment.light

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmLightPlugin : LightPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> = emptyFlow()
}

actual fun createLightPlugin(): LightPlugin = JvmLightPlugin()
