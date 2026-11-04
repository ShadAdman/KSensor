package com.ksensor.plugins.states.bluetooth.discoveries

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmBleDiscoveriesStatusPlugin : BleDiscoveriesStatusPlugin {
    override val currentState: KSensorResponse<StateData.BleDiscoversStatus>
        get() = KSensorResponse(StateData.BleDiscoversStatus(emptyList()))

    override fun observe(): Flow<KSensorResponse<StateData.BleDiscoversStatus>> = emptyFlow()
}

actual fun createBleDiscoveriesStatusPlugin(): BleDiscoveriesStatusPlugin = JvmBleDiscoveriesStatusPlugin()
