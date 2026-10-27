package com.ksensor.plugins.sensors.environment.barometer

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmBarometerPlugin : BarometerPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> = emptyFlow()
}

actual fun createBarometerPlugin(): BarometerPlugin = JvmBarometerPlugin()
