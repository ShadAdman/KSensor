package com.ksensor.plugins.sensors.interaction.touch

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface TouchPlugin : SensorPlugin<SensorData.TouchGestures> {
    override val id: PluginId get() = PluginId.TOUCH_GESTURES
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun touchGestures(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.TouchGestures>> = observe(config)
}

expect fun createTouchPlugin(): TouchPlugin
