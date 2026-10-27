package com.ksensor.plugins.states.system.powersave

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface PowerSaveStatusPlugin : StatePlugin<StateData.PowerSaveStatus> {
    override val id: PluginId get() = PluginId.POWER_SAVE
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createPowerSaveStatusPlugin(): PowerSaveStatusPlugin
