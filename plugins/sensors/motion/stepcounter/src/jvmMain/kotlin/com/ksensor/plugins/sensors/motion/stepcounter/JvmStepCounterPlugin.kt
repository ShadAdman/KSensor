package com.ksensor.plugins.sensors.motion.stepcounter

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmStepCounterPlugin : StepCounterPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.StepCounter>> = emptyFlow()
}

actual fun createStepCounterPlugin(): StepCounterPlugin = JvmStepCounterPlugin()
