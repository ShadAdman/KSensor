package com.ksensor.plugins.sensors.positioning.heading

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface HeadingPlugin : SensorPlugin<SensorData.Heading> {
    override val id: PluginId get() = PluginId.HEADING
    override val requiredPermissions: List<Permission> get() = listOf(Permission.LOCATION)
    fun heading(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Heading>> = observe(config)
}

expect fun createHeadingPlugin(): HeadingPlugin
