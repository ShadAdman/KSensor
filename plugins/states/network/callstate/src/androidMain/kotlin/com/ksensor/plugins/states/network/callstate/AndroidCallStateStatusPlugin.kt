package com.ksensor.plugins.states.network.callstate

import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class AndroidCallStateStatusPlugin : CallStateStatusPlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.CallStateStatus>
        get() = KSensorResponse(CallStateMonitoring(KSensorContext.get()) {}.getCurrentStatus())

    private val callStateFlow by lazy {
        callbackFlow {
            val monitoring = CallStateMonitoring(KSensorContext.get()) { status ->
                trySend(KSensorResponse(status))
            }
            monitoring.startMonitoring()
            awaitClose { monitoring.stopMonitoring() }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.CallStateStatus>> = callStateFlow
}

actual fun createCallStateStatusPlugin(): CallStateStatusPlugin = AndroidCallStateStatusPlugin()
