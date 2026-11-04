package com.ksensor.plugins.states.system.brightness

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosBrightnessStatusPlugin : BrightnessStatusPlugin {
    private val receiver = BrightnessReceiver {}

    override val currentState: KSensorResponse<StateData.BrightnessStatus>
        get() = KSensorResponse(receiver.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.BrightnessStatus>> = callbackFlow {
        val obs = BrightnessReceiver { trySend(KSensorResponse(it)) }
        obs.register()
        awaitClose { obs.unregister() }
    }
}

actual fun createBrightnessStatusPlugin(): BrightnessStatusPlugin = IosBrightnessStatusPlugin()
