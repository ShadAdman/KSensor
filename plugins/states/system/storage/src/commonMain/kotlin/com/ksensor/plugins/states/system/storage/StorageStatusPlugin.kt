package com.ksensor.plugins.states.system.storage

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface StorageStatusPlugin : StatePlugin<StateData.StorageStatus> {
    override val id: PluginId get() = PluginId.STORAGE
    override val requiredPermissions: List<Permission> get() = emptyList()
    fun storage(interval: Duration = 3.seconds): StatePlugin<StateData.StorageStatus> = this
}

expect fun createStorageStatusPlugin(interval: Duration = 3.seconds): StorageStatusPlugin
