package com.ksensor.plugins.states.network.callstate

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface CallStateStatusPlugin : StatePlugin<StateData.CallStateStatus> {
    override val id: PluginId get() = PluginId.CALL_STATE
    override val requiredPermissions: List<Permission> get() = listOf(Permission.READ_PHONE_STATE)
}

expect fun createCallStateStatusPlugin(): CallStateStatusPlugin
