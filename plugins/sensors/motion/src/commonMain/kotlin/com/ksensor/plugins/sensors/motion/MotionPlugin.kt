package com.ksensor.plugins.sensors.motion

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface MotionPlugin : KSensorPlugin {
    fun accelerometer(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Accelerometer>
    fun gyroscope(config: SensorConfig = SensorConfig.Default): Flow<SensorData.Gyroscope>
    fun stepCounter(config: SensorConfig = SensorConfig.Default): Flow<SensorData.StepCounter>
    fun stepDetector(config: SensorConfig = SensorConfig.Default): Flow<SensorData.StepDetector>
}

expect fun createMotionPlugin(): MotionPlugin
