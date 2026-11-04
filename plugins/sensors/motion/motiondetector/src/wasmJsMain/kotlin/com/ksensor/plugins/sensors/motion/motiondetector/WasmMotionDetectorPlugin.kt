package com.ksensor.plugins.sensors.motion.motiondetector

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmMotionDetectorPlugin : MotionDetectorPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.MotionDetector>> = emptyFlow()
}

actual fun createMotionDetectorPlugin(): MotionDetectorPlugin = WasmMotionDetectorPlugin()
