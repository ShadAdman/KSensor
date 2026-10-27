package com.ksensor.plugins.states.system.lock

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmLockStatusPlugin : LockStatusPlugin {
    override val currentState: KSensorResponse<StateData.LockStatus>
        get() = KSensorResponse(StateData.LockStatus(false))

    override fun observe(): Flow<KSensorResponse<StateData.LockStatus>> = emptyFlow()
}

actual fun createLockStatusPlugin(): LockStatusPlugin = WasmLockStatusPlugin()
