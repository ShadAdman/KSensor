package com.ksensor.plugins.states.bluetooth.discoveries

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData

interface BleDiscoveriesStatusPlugin : StatePlugin<StateData.BleDiscoversStatus> {
    override val id: PluginId get() = PluginId.BLE_DISCOVERIES
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BLUETOOTH)
}

expect fun createBleDiscoveriesStatusPlugin(): BleDiscoveriesStatusPlugin
