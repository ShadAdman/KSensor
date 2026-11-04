package com.ksensor.plugins.sensors.positioning.magnetometer

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface MagnetometerPlugin : SensorPlugin<SensorData.Magnetometer> {
    override val id: PluginId get() = PluginId.MAGNETOMETER
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun magnetometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Magnetometer>> = observe(config)
}

expect fun createMagnetometerPlugin(): MagnetometerPlugin
