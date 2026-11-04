package com.ksensor.plugins.states.system.volume

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmVolumeStatusPlugin : VolumeStatusPlugin {
    override val currentState: KSensorResponse<StateData.VolumeStatus>
        get() = KSensorResponse(StateData.VolumeStatus(100))

    override fun observe(): Flow<KSensorResponse<StateData.VolumeStatus>> = emptyFlow()
}

actual fun createVolumeStatusPlugin(): VolumeStatusPlugin = JvmVolumeStatusPlugin()
