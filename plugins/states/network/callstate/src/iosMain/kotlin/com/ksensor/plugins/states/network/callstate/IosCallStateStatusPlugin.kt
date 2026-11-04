package com.ksensor.plugins.states.network.callstate

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosCallStateStatusPlugin : CallStateStatusPlugin {
    override val currentState: KSensorResponse<StateData.CallStateStatus>
        get() = KSensorResponse(CallStateMonitoring {}.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.CallStateStatus>> = callbackFlow {
        val monitoring = CallStateMonitoring { status ->
            trySend(KSensorResponse(status))
        }
        monitoring.startMonitoring()
        awaitClose { monitoring.stopMonitoring() }
    }
}

actual fun createCallStateStatusPlugin(): CallStateStatusPlugin = IosCallStateStatusPlugin()
