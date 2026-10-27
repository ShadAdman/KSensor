package com.ksensor.plugins.states.system.screen

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmScreenStatusPlugin : ScreenStatusPlugin {
    override val currentState: KSensorResponse<StateData.ScreenStatus>
        get() = KSensorResponse(StateData.ScreenStatus(true))

    override fun observe(): Flow<KSensorResponse<StateData.ScreenStatus>> = emptyFlow()
}

actual fun createScreenStatusPlugin(): ScreenStatusPlugin = WasmScreenStatusPlugin()
