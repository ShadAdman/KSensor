package com.ksensor.plugins.states.network.connectivity

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmConnectivityStatusPlugin : ConnectivityStatusPlugin {
    override val currentState: KSensorResponse<StateData.ConnectivityStatus>
        get() = KSensorResponse(StateData.ConnectivityStatus(false))

    override fun observe(): Flow<KSensorResponse<StateData.ConnectivityStatus>> = emptyFlow()
}

actual fun createConnectivityStatusPlugin(): ConnectivityStatusPlugin = JvmConnectivityStatusPlugin()
