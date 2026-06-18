package com.ksensor.plugins.sensors.environment

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface EnvironmentPlugin : KSensorPlugin {
    fun barometer(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Barometer>
    fun light(config: SensorConfig = SensorConfig.Default): Flow<SensorData.LightIlluminance>
    fun proximity(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Proximity>
}

expect fun createEnvironmentPlugin(): EnvironmentPlugin
