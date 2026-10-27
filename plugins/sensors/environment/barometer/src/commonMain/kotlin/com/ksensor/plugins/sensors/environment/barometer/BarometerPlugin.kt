package com.ksensor.plugins.sensors.environment.barometer

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface BarometerPlugin : SensorPlugin<SensorData.Barometer> {
    override val id: PluginId get() = PluginId.BAROMETER
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun barometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Barometer>> = observe(config)
}

expect fun createBarometerPlugin(): BarometerPlugin
