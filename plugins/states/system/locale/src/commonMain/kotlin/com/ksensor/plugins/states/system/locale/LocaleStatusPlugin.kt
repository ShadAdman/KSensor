package com.ksensor.plugins.states.system.locale

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface LocaleStatusPlugin : StatePlugin<StateData.LocaleStatus> {
    override val id: PluginId get() = PluginId.LOCALE
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createLocaleStatusPlugin(): LocaleStatusPlugin
