package com.ksensor.plugins.sensors.motion.gyroscope

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface GyroscopePlugin : SensorPlugin<SensorData.Gyroscope> {
    override val id: PluginId get() = PluginId.GYROSCOPE
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun gyroscope(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Gyroscope>> = observe(config)
}

expect fun createGyroscopePlugin(): GyroscopePlugin
