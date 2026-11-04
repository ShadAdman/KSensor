package com.ksensor.plugins.states.system.lock

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface LockStatusPlugin : StatePlugin<StateData.LockStatus> {
    override val id: PluginId get() = PluginId.LOCK
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createLockStatusPlugin(): LockStatusPlugin
