package com.ksensor.plugins.sensors.environment.light

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface LightPlugin : SensorPlugin<SensorData.LightIlluminance> {
    override val id: PluginId get() = PluginId.LIGHT
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun light(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.LightIlluminance>> = observe(config)
}

expect fun createLightPlugin(): LightPlugin
