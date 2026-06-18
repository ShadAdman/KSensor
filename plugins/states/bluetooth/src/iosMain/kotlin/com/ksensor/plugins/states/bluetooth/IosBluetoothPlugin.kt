package com.ksensor.plugins.states.bluetooth

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosBluetoothPlugin : BluetoothPlugin {
    override val id: String = "ksensor.states.bluetooth"
    override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)

    override fun connections(): StatePlugin<StateData.BleConnectionStatus> = object : StatePlugin<StateData.BleConnectionStatus> {
        override val id: String = "${this@IosBluetoothPlugin.id}.connections"
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: StateData.BleConnectionStatus = StateData.BleConnectionStatus(emptyList())
        override fun observe(): Flow<StateData.BleConnectionStatus> = emptyFlow()
    }

    override fun discoveries(): StatePlugin<StateData.BleDiscoversStatus> = object : StatePlugin<StateData.BleDiscoversStatus> {
        override val id: String = "${this@IosBluetoothPlugin.id}.discoveries"
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: StateData.BleDiscoversStatus = StateData.BleDiscoversStatus(emptyList())
        override fun observe(): Flow<StateData.BleDiscoversStatus> = emptyFlow()
    }
}

actual fun createBluetoothPlugin(): BluetoothPlugin = IosBluetoothPlugin()
