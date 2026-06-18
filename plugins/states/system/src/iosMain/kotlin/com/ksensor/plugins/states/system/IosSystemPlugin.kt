package com.ksensor.plugins.states.system

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosSystemPlugin : SystemPlugin {
    override val id: String = "ksensor.states.system"
    override val requiredPermissions: List<Permission> = emptyList()

    override fun battery(): StatePlugin<StateData.BatteryStatus> = object : StatePlugin<StateData.BatteryStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.battery"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.BatteryStatus = StateData.BatteryStatus(null, StateData.BatteryStatus.ChargingState.UNKNOWN, null, null)
        override fun observe(): Flow<StateData.BatteryStatus> = emptyFlow()
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = TODO()
    override fun locale(): StatePlugin<StateData.LocaleStatus> = TODO()
    override fun screen(): StatePlugin<StateData.ScreenStatus> = TODO()
    override fun lock(): StatePlugin<StateData.LockStatus> = TODO()
}

actual fun createSystemPlugin(): SystemPlugin = IosSystemPlugin()
