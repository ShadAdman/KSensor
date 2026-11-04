package com.ksensor.plugins.sensors.positioning.orientation

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface OrientationPlugin : SensorPlugin<SensorData.Orientation> {
    override val id: PluginId get() = PluginId.ORIENTATION
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun orientation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Orientation>> = observe(config)
}

expect fun createOrientationPlugin(): OrientationPlugin
