package com.ksensor.plugins.states.lifecycle

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosLifecyclePlugin : LifecyclePlugin {
    override val id: String = "ksensor.states.lifecycle"
    override val requiredPermissions: List<Permission> = emptyList()

    override fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus> = object : StatePlugin<StateData.AppVisibilityStatus> {
        override val id: String = "${this@IosLifecyclePlugin.id}.visibility"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.AppVisibilityStatus = StateData.AppVisibilityStatus(true)
        override fun observe(): Flow<StateData.AppVisibilityStatus> = emptyFlow()
    }
}

actual fun createLifecyclePlugin(): LifecyclePlugin = IosLifecyclePlugin()
