package com.ksensor.plugins.sensors.motion

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.motion.accelerometer.AccelerometerPlugin
import com.ksensor.plugins.sensors.motion.accelerometer.createAccelerometerPlugin
import com.ksensor.plugins.sensors.motion.gyroscope.GyroscopePlugin
import com.ksensor.plugins.sensors.motion.gyroscope.createGyroscopePlugin
import com.ksensor.plugins.sensors.motion.motiondetector.MotionDetectorPlugin
import com.ksensor.plugins.sensors.motion.motiondetector.createMotionDetectorPlugin
import com.ksensor.plugins.sensors.motion.stepcounter.StepCounterPlugin
import com.ksensor.plugins.sensors.motion.stepcounter.createStepCounterPlugin
import kotlinx.coroutines.flow.Flow

interface MotionPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.MOTION
    override val requiredPermissions: List<Permission>
        get() = listOf(Permission.ACTIVITY_RECOGNITION)

    fun accelerometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Accelerometer>>
    fun gyroscope(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Gyroscope>>
    fun stepCounter(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.StepCounter>>
    fun motionDetector(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.MotionDetector>>
}

class DefaultMotionPlugin(
    private val accelerometerPlugin: AccelerometerPlugin = createAccelerometerPlugin(),
    private val gyroscopePlugin: GyroscopePlugin = createGyroscopePlugin(),
    private val stepCounterPlugin: StepCounterPlugin = createStepCounterPlugin(),
    private val motionDetectorPlugin: MotionDetectorPlugin = createMotionDetectorPlugin()
) : MotionPlugin {
    override fun accelerometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Accelerometer>> =
        accelerometerPlugin.observe(config)

    override fun gyroscope(config: SensorConfig): Flow<KSensorResponse<SensorData.Gyroscope>> =
        gyroscopePlugin.observe(config)

    override fun stepCounter(config: SensorConfig): Flow<KSensorResponse<SensorData.StepCounter>> =
        stepCounterPlugin.observe(config)

    override fun motionDetector(config: SensorConfig): Flow<KSensorResponse<SensorData.MotionDetector>> =
        motionDetectorPlugin.observe(config)
}

fun createMotionPlugin(): MotionPlugin = DefaultMotionPlugin()
