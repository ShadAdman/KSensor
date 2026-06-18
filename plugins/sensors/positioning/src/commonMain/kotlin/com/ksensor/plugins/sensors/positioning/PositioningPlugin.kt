package com.ksensor.plugins.sensors.positioning

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface PositioningPlugin : KSensorPlugin {
    fun location(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Location>
    fun magnetometer(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Magnetometer>
    fun orientation(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Orientation>
}

expect fun createPositioningPlugin(): PositioningPlugin
