package com.ksensor.plugins.sensors.motion.motiondetector

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface MotionDetectorPlugin : SensorPlugin<SensorData.MotionDetector> {
    override val id: PluginId get() = PluginId.MOTION_DETECTOR
    override val requiredPermissions: List<Permission> get() = listOf(Permission.ACTIVITY_RECOGNITION)
    fun motionDetector(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.MotionDetector>> = observe(config)
}

expect fun createMotionDetectorPlugin(): MotionDetectorPlugin
