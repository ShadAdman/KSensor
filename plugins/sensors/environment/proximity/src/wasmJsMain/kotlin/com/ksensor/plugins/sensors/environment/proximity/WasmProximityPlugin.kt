package com.ksensor.plugins.sensors.environment.proximity

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmProximityPlugin : ProximityPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> = emptyFlow()
}

actual fun createProximityPlugin(): ProximityPlugin = WasmProximityPlugin()
