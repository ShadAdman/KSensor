package com.ksensor.plugins.sensors.motion.gyroscope

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmGyroscopePlugin : GyroscopePlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Gyroscope>> = emptyFlow()
}

actual fun createGyroscopePlugin(): GyroscopePlugin = JvmGyroscopePlugin()
