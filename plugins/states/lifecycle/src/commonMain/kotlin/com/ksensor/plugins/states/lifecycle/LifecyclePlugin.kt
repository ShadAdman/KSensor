package com.ksensor.plugins.states.lifecycle

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import com.ksensor.plugins.states.lifecycle.appvisibility.AppVisibilityStatusPlugin
import com.ksensor.plugins.states.lifecycle.appvisibility.createAppVisibilityStatusPlugin

interface LifecyclePlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.LIFECYCLE
    override val requiredPermissions: List<Permission> get() = emptyList()

    fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus>
}

class DefaultLifecyclePlugin(
    private val appVisibilityPlugin: AppVisibilityStatusPlugin = createAppVisibilityStatusPlugin()
) : LifecyclePlugin {
    override fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus> = appVisibilityPlugin
}

fun createLifecyclePlugin(): LifecyclePlugin = DefaultLifecyclePlugin()
