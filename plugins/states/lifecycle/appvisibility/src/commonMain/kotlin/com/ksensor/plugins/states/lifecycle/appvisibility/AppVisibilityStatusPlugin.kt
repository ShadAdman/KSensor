package com.ksensor.plugins.states.lifecycle.appvisibility

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface AppVisibilityStatusPlugin : StatePlugin<StateData.AppVisibilityStatus> {
    override val id: PluginId get() = PluginId.APP_VISIBILITY
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createAppVisibilityStatusPlugin(): AppVisibilityStatusPlugin
