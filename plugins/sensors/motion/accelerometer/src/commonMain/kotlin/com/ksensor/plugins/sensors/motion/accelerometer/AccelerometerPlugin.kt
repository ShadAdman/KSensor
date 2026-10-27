package com.ksensor.plugins.sensors.motion.accelerometer

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface AccelerometerPlugin : SensorPlugin<SensorData.Accelerometer> {
    override val id: PluginId get() = PluginId.ACCELEROMETER
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun accelerometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Accelerometer>> = observe(config)
}

expect fun createAccelerometerPlugin(): AccelerometerPlugin
