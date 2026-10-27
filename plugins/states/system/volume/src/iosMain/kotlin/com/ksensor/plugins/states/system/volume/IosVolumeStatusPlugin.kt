package com.ksensor.plugins.states.system.volume

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosVolumeStatusPlugin : VolumeStatusPlugin {
    private val receiver = VolumeReceiver {}

    override val currentState: KSensorResponse<StateData.VolumeStatus>
        get() = KSensorResponse(StateData.VolumeStatus(receiver.getCurrentVolume()))

    override fun observe(): Flow<KSensorResponse<StateData.VolumeStatus>> = callbackFlow {
        val obs = VolumeReceiver { trySend(KSensorResponse(StateData.VolumeStatus(it))) }
        obs.register()
        awaitClose { obs.unregister() }
    }
}

actual fun createVolumeStatusPlugin(): VolumeStatusPlugin = IosVolumeStatusPlugin()
