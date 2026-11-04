package com.ksensor.plugins.states.network.activenetwork

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface ActiveNetworkStatusPlugin : StatePlugin<StateData.CurrentActiveNetwork> {
    override val id: PluginId get() = PluginId.ACTIVE_NETWORK
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createActiveNetworkStatusPlugin(): ActiveNetworkStatusPlugin
