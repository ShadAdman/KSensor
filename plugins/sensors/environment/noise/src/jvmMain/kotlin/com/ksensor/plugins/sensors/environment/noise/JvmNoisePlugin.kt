package com.ksensor.plugins.sensors.environment.noise

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmNoisePlugin : NoisePlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Noise>> = emptyFlow()
    override fun noise(config: SensorConfig, loudThresholdDb: Float): Flow<KSensorResponse<SensorData.Noise>> = emptyFlow()
}

actual fun createNoisePlugin(): NoisePlugin = JvmNoisePlugin()
