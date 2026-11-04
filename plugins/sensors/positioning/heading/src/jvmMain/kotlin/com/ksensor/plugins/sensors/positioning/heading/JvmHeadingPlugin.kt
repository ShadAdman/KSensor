package com.ksensor.plugins.sensors.positioning.heading

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmHeadingPlugin : HeadingPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Heading>> = emptyFlow()
}

actual fun createHeadingPlugin(): HeadingPlugin = JvmHeadingPlugin()
