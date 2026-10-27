package com.ksensor.plugins.states.bluetooth.connections

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmBleConnectionsStatusPlugin : BleConnectionsStatusPlugin {
    override val currentState: KSensorResponse<StateData.BleConnectionStatus>
        get() = KSensorResponse(StateData.BleConnectionStatus(emptyList()))

    override fun observe(): Flow<KSensorResponse<StateData.BleConnectionStatus>> = emptyFlow()
}

actual fun createBleConnectionsStatusPlugin(): BleConnectionsStatusPlugin = WasmBleConnectionsStatusPlugin()
