package com.ksensor.plugins.states.bluetooth.connections

import android.content.Context
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

class AndroidBleConnectionsStatusPlugin : BleConnectionsStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.BleConnectionStatus>
        get() = KSensorResponse(StateData.BleConnectionStatus(emptyList()))

    private val connectionsFlow by lazy {
        callbackFlow {
            val receiver = BleConnectionReceiver(context) { trySend(KSensorResponse(it)) }
            receiver.register()
            awaitClose { receiver.unregister() }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.BleConnectionStatus>> = connectionsFlow
}

actual fun createBleConnectionsStatusPlugin(): BleConnectionsStatusPlugin = AndroidBleConnectionsStatusPlugin()
