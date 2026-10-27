package com.ksensor.plugins.sensors.positioning.location

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface LocationPlugin : SensorPlugin<SensorData.Location> {
    override val id: PluginId get() = PluginId.LOCATION
    override val requiredPermissions: List<Permission> get() = listOf(Permission.LOCATION)
    fun location(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Location>> = observe(config)
}

expect fun createLocationPlugin(): LocationPlugin
