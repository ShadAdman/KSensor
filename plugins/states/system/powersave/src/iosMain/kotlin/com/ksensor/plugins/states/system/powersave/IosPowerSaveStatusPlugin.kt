package com.ksensor.plugins.states.system.powersave

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosPowerSaveStatusPlugin : PowerSaveStatusPlugin {
    private val receiver = PowerSaveReceiver {}

    override val currentState: KSensorResponse<StateData.PowerSaveStatus>
        get() = KSensorResponse(receiver.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.PowerSaveStatus>> = callbackFlow {
        val obs = PowerSaveReceiver { trySend(KSensorResponse(it)) }
        obs.register()
        awaitClose { obs.unregister() }
    }
}

actual fun createPowerSaveStatusPlugin(): PowerSaveStatusPlugin = IosPowerSaveStatusPlugin()
