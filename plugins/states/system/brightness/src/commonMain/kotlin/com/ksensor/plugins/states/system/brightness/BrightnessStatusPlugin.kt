package com.ksensor.plugins.states.system.brightness

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface BrightnessStatusPlugin : StatePlugin<StateData.BrightnessStatus> {
    override val id: PluginId get() = PluginId.BRIGHTNESS
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createBrightnessStatusPlugin(): BrightnessStatusPlugin
