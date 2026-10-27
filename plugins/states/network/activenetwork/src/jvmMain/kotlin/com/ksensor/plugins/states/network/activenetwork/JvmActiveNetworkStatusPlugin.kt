package com.ksensor.plugins.states.network.activenetwork

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmActiveNetworkStatusPlugin : ActiveNetworkStatusPlugin {
    override val currentState: KSensorResponse<StateData.CurrentActiveNetwork>
        get() = KSensorResponse(StateData.CurrentActiveNetwork(StateData.CurrentActiveNetwork.ActiveNetwork.NONE))

    override fun observe(): Flow<KSensorResponse<StateData.CurrentActiveNetwork>> = emptyFlow()
}

actual fun createActiveNetworkStatusPlugin(): ActiveNetworkStatusPlugin = JvmActiveNetworkStatusPlugin()
