package com.ksensor.plugins.sensors.health

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.health.heart.HeartRatePlugin
import com.ksensor.plugins.sensors.health.heart.createHeartRatePlugin
import kotlinx.coroutines.flow.Flow

interface HealthPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.HEALTH
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BODY_SENSORS, Permission.CAMERA)

    fun heartRate(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.HeartRate>>
}

class DefaultHealthPlugin(
    private val heartRatePlugin: HeartRatePlugin = createHeartRatePlugin()
) : HealthPlugin {
    override fun heartRate(config: SensorConfig): Flow<KSensorResponse<SensorData.HeartRate>> =
        heartRatePlugin.observe(config)
}

fun createHealthPlugin(): HealthPlugin = DefaultHealthPlugin()
