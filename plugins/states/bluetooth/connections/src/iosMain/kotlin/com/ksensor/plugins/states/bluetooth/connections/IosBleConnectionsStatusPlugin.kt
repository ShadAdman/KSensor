package com.ksensor.plugins.states.bluetooth.connections

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosBleConnectionsStatusPlugin : BleConnectionsStatusPlugin {
    override val currentState: KSensorResponse<StateData.BleConnectionStatus>
        get() = KSensorResponse(StateData.BleConnectionStatus(emptyList()))

    override fun observe(): Flow<KSensorResponse<StateData.BleConnectionStatus>> = callbackFlow {
        val receiver = BleConnectionReceiver { trySend(KSensorResponse(it)) }
        receiver.register()
        awaitClose { receiver.unregister() }
    }
}

actual fun createBleConnectionsStatusPlugin(): BleConnectionsStatusPlugin = IosBleConnectionsStatusPlugin()
