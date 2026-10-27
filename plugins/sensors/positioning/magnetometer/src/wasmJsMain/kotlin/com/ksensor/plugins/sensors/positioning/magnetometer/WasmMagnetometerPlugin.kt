package com.ksensor.plugins.sensors.positioning.magnetometer

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmMagnetometerPlugin : MagnetometerPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Magnetometer>> = emptyFlow()
}

actual fun createMagnetometerPlugin(): MagnetometerPlugin = WasmMagnetometerPlugin()
