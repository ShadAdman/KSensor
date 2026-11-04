package com.ksensor.plugins.sensors.environment

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.environment.barometer.BarometerPlugin
import com.ksensor.plugins.sensors.environment.barometer.createBarometerPlugin
import com.ksensor.plugins.sensors.environment.light.LightPlugin
import com.ksensor.plugins.sensors.environment.light.createLightPlugin
import com.ksensor.plugins.sensors.environment.noise.NoisePlugin
import com.ksensor.plugins.sensors.environment.noise.createNoisePlugin
import com.ksensor.plugins.sensors.environment.proximity.ProximityPlugin
import com.ksensor.plugins.sensors.environment.proximity.createProximityPlugin
import kotlinx.coroutines.flow.Flow

interface EnvironmentPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.ENVIRONMENT
    override val requiredPermissions: List<Permission> get() = listOf(Permission.RECORD_AUDIO)

    fun barometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Barometer>>
    fun light(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.LightIlluminance>>
    fun proximity(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Proximity>>
    fun noise(config: SensorConfig = SensorConfig.Default, loudThresholdDb: Float = 80f): Flow<KSensorResponse<SensorData.Noise>>
}

class DefaultEnvironmentPlugin(
    private val barometerPlugin: BarometerPlugin = createBarometerPlugin(),
    private val lightPlugin: LightPlugin = createLightPlugin(),
    private val proximityPlugin: ProximityPlugin = createProximityPlugin(),
    private val noisePlugin: NoisePlugin = createNoisePlugin()
) : EnvironmentPlugin {
    override fun barometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> =
        barometerPlugin.observe(config)

    override fun light(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> =
        lightPlugin.observe(config)

    override fun proximity(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> =
        proximityPlugin.observe(config)

    override fun noise(config: SensorConfig, loudThresholdDb: Float): Flow<KSensorResponse<SensorData.Noise>> =
        noisePlugin.noise(config, loudThresholdDb)
}

fun createEnvironmentPlugin(): EnvironmentPlugin = DefaultEnvironmentPlugin()
