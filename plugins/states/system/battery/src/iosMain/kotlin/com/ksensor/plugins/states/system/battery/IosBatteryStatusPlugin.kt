package com.ksensor.plugins.states.system.battery

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosBatteryStatusPlugin : BatteryStatusPlugin {
    private val receiver = BatteryStateReceiver {}

    override val currentState: KSensorResponse<StateData.BatteryStatus>
        get() = KSensorResponse(receiver.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.BatteryStatus>> = callbackFlow {
        val obs = BatteryStateReceiver { trySend(KSensorResponse(it)) }
        obs.register()
        awaitClose { obs.unregister() }
    }
}

actual fun createBatteryStatusPlugin(): BatteryStatusPlugin = IosBatteryStatusPlugin()
