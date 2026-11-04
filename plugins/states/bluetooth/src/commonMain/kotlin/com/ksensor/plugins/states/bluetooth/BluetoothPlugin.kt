package com.ksensor.plugins.states.bluetooth

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import com.ksensor.plugins.states.bluetooth.audiodevices.BleAudioDevicesStatusPlugin
import com.ksensor.plugins.states.bluetooth.audiodevices.createBleAudioDevicesStatusPlugin
import com.ksensor.plugins.states.bluetooth.connections.BleConnectionsStatusPlugin
import com.ksensor.plugins.states.bluetooth.connections.createBleConnectionsStatusPlugin
import com.ksensor.plugins.states.bluetooth.discoveries.BleDiscoveriesStatusPlugin
import com.ksensor.plugins.states.bluetooth.discoveries.createBleDiscoveriesStatusPlugin

interface BluetoothPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.BLUETOOTH
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BLUETOOTH)

    fun connections(): StatePlugin<StateData.BleConnectionStatus>
    fun discoveries(): StatePlugin<StateData.BleDiscoversStatus>
    fun audioDevices(): StatePlugin<StateData.BleConnectionStatus>
}

class DefaultBluetoothPlugin(
    private val connectionsPlugin: BleConnectionsStatusPlugin = createBleConnectionsStatusPlugin(),
    private val discoveriesPlugin: BleDiscoveriesStatusPlugin = createBleDiscoveriesStatusPlugin(),
    private val audioDevicesPlugin: BleAudioDevicesStatusPlugin = createBleAudioDevicesStatusPlugin()
) : BluetoothPlugin {
    override fun connections(): StatePlugin<StateData.BleConnectionStatus> = connectionsPlugin
    override fun discoveries(): StatePlugin<StateData.BleDiscoversStatus> = discoveriesPlugin
    override fun audioDevices(): StatePlugin<StateData.BleConnectionStatus> = audioDevicesPlugin
}

fun createBluetoothPlugin(): BluetoothPlugin = DefaultBluetoothPlugin()
