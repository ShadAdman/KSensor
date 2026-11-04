package com.ksensor.plugins.states.system.powersave

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmPowerSaveStatusPlugin : PowerSaveStatusPlugin {
    override val currentState: KSensorResponse<StateData.PowerSaveStatus>
        get() = KSensorResponse(StateData.PowerSaveStatus(false))

    override fun observe(): Flow<KSensorResponse<StateData.PowerSaveStatus>> = emptyFlow()
}

actual fun createPowerSaveStatusPlugin(): PowerSaveStatusPlugin = JvmPowerSaveStatusPlugin()
