package com.ksensor.plugins.states.system.volume

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface VolumeStatusPlugin : StatePlugin<StateData.VolumeStatus> {
    override val id: PluginId get() = PluginId.VOLUME
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createVolumeStatusPlugin(): VolumeStatusPlugin
