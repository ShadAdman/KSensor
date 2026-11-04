package com.ksensor.plugins.states.system.battery

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface BatteryStatusPlugin : StatePlugin<StateData.BatteryStatus> {
    override val id: PluginId get() = PluginId.BATTERY
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createBatteryStatusPlugin(): BatteryStatusPlugin
