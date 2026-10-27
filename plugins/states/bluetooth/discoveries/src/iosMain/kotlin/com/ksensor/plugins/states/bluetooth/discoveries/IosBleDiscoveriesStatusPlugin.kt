package com.ksensor.plugins.states.bluetooth.discoveries

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosBleDiscoveriesStatusPlugin : BleDiscoveriesStatusPlugin {
    override val currentState: KSensorResponse<StateData.BleDiscoversStatus>
        get() = KSensorResponse(StateData.BleDiscoversStatus(emptyList()))

    override fun observe(): Flow<KSensorResponse<StateData.BleDiscoversStatus>> = callbackFlow {
        val receiver = BleDiscoversReceiver({ trySend(KSensorResponse(it)) }, { close(it) })
        receiver.register()
        awaitClose { receiver.unregister() }
    }
}

actual fun createBleDiscoveriesStatusPlugin(): BleDiscoveriesStatusPlugin = IosBleDiscoveriesStatusPlugin()
