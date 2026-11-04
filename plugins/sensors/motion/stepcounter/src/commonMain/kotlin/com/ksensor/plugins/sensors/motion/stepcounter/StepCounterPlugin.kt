package com.ksensor.plugins.sensors.motion.stepcounter

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface StepCounterPlugin : SensorPlugin<SensorData.StepCounter> {
    override val id: PluginId get() = PluginId.STEP_COUNTER
    override val requiredPermissions: List<Permission> get() = listOf(Permission.ACTIVITY_RECOGNITION)
    fun stepCounter(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.StepCounter>> = observe(config)
}

expect fun createStepCounterPlugin(): StepCounterPlugin
