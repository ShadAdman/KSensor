package com.ksensor.plugins.sensors.environment.barometer

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmBarometerPlugin : BarometerPlugin {
    override val isSupported: Boolean get() = false
    override val unsupportedReason: String get() = "Barometer sensor is not supported on platform Web/WasmJs"

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> = emptyFlow()
}

actual fun createBarometerPlugin(): BarometerPlugin = WasmBarometerPlugin()
