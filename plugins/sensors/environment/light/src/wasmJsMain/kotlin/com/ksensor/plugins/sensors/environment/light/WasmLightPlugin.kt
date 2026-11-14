package com.ksensor.plugins.sensors.environment.light

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmLightPlugin : LightPlugin {
    override val isSupported: Boolean get() = false
    override val unsupportedReason: String get() = "Light sensor is not supported on platform Web/WasmJs"

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> = emptyFlow()
}

actual fun createLightPlugin(): LightPlugin = WasmLightPlugin()
