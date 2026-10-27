package com.ksensor.plugins.states.network.callstate

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmCallStateStatusPlugin : CallStateStatusPlugin {
    override val currentState: KSensorResponse<StateData.CallStateStatus>
        get() = KSensorResponse(StateData.CallStateStatus(StateData.CallStateStatus.CallState.IDLE, false))

    override fun observe(): Flow<KSensorResponse<StateData.CallStateStatus>> = emptyFlow()
}

actual fun createCallStateStatusPlugin(): CallStateStatusPlugin = JvmCallStateStatusPlugin()
