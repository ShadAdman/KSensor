package com.ksensor.plugins.sensors.interaction.touch

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmTouchPlugin : TouchPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> = emptyFlow()
}

actual fun createTouchPlugin(): TouchPlugin = JvmTouchPlugin()
