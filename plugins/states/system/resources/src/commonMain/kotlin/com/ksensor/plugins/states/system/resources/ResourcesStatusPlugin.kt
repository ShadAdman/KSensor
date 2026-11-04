package com.ksensor.plugins.states.system.resources

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ResourcesStatusPlugin : StatePlugin<StateData.ResourcesStatus> {
    override val id: PluginId get() = PluginId.RESOURCES
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun resources(interval: Duration = 3.seconds): StatePlugin<StateData.ResourcesStatus> = this
}

expect fun createResourcesStatusPlugin(interval: Duration = 3.seconds): ResourcesStatusPlugin
