package com.ksensor.plugins.states.system.battery

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmBatteryStatusPlugin : BatteryStatusPlugin {
    override val currentState: KSensorResponse<StateData.BatteryStatus>
        get() = KSensorResponse(StateData.BatteryStatus(100, StateData.BatteryStatus.ChargingState.UNKNOWN, null, null))

    override fun observe(): Flow<KSensorResponse<StateData.BatteryStatus>> = emptyFlow()
}

actual fun createBatteryStatusPlugin(): BatteryStatusPlugin = WasmBatteryStatusPlugin()
