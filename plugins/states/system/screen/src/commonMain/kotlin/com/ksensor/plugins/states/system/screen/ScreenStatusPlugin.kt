package com.ksensor.plugins.states.system.screen

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface ScreenStatusPlugin : StatePlugin<StateData.ScreenStatus> {
    override val id: PluginId get() = PluginId.SCREEN
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createScreenStatusPlugin(): ScreenStatusPlugin
