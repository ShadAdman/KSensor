package com.ksensor.plugins.states.bluetooth.connections

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface BleConnectionsStatusPlugin : StatePlugin<StateData.BleConnectionStatus> {
    override val id: PluginId get() = PluginId.BLE_CONNECTIONS
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BLUETOOTH)
}

expect fun createBleConnectionsStatusPlugin(): BleConnectionsStatusPlugin
