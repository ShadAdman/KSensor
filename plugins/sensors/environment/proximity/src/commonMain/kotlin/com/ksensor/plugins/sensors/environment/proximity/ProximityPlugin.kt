package com.ksensor.plugins.sensors.environment.proximity

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface ProximityPlugin : SensorPlugin<SensorData.Proximity> {
    override val id: PluginId get() = PluginId.PROXIMITY
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun proximity(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Proximity>> = observe(config)
}

expect fun createProximityPlugin(): ProximityPlugin
