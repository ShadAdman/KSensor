package com.ksensor.plugins.sensors.health.heart

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface HeartRatePlugin : SensorPlugin<SensorData.HeartRate> {
    override val id: PluginId get() = PluginId.HEART_RATE
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BODY_SENSORS, Permission.CAMERA)
    fun heartRate(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.HeartRate>> = observe(config)
}

expect fun createHeartRatePlugin(): HeartRatePlugin
