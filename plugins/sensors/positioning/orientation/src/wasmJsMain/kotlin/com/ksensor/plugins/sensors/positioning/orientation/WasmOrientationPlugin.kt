package com.ksensor.plugins.sensors.positioning.orientation

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmOrientationPlugin : OrientationPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Orientation>> = emptyFlow()
}

actual fun createOrientationPlugin(): OrientationPlugin = WasmOrientationPlugin()
