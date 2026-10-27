package com.ksensor.plugins.sensors.environment.noise

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface NoisePlugin : SensorPlugin<SensorData.Noise> {
    override val id: PluginId get() = PluginId.NOISE
    override val requiredPermissions: List<Permission> get() = listOf(Permission.RECORD_AUDIO)
    fun noise(config: SensorConfig = SensorConfig.Default, loudThresholdDb: Float = 80f): Flow<KSensorResponse<SensorData.Noise>>
}

expect fun createNoisePlugin(): NoisePlugin
