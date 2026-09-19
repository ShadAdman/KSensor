package com.ksensor.plugins.sensors.environment

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface EnvironmentPlugin : KSensorPlugin {
    fun barometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Barometer>>
    fun light(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.LightIlluminance>>
    fun proximity(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Proximity>>
    fun noise(config: SensorConfig = SensorConfig.Default, loudThresholdDb: Float = 80f): Flow<KSensorResponse<SensorData.Noise>>
}

expect fun createEnvironmentPlugin(): EnvironmentPlugin
