package com.ksensor.plugins.sensors.positioning.location

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmLocationPlugin : LocationPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Location>> = emptyFlow()
}

actual fun createLocationPlugin(): LocationPlugin = WasmLocationPlugin()
