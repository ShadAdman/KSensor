package com.ksensor.plugins.sensors.motion.accelerometer

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmAccelerometerPlugin : AccelerometerPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Accelerometer>> = emptyFlow()
}

actual fun createAccelerometerPlugin(): AccelerometerPlugin = JvmAccelerometerPlugin()
