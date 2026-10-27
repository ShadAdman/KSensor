package com.ksensor.plugins.sensors.interaction.scroll

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface ScrollPlugin : StatePlugin<StateData.ScrollStatus> {
    override val id: PluginId get() = PluginId.SCROLL_STATUS
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createScrollPlugin(): ScrollPlugin
