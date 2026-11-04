package com.ksensor.plugins.sensors.positioning.locationstatus

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface LocationStatusPlugin : StatePlugin<StateData.LocationStatus> {
    override val id: PluginId get() = PluginId.LOCATION_STATUS
    override val requiredPermissions: List<Permission> get() = emptyList()
}

expect fun createLocationStatusPlugin(): LocationStatusPlugin
