package com.ksensor.plugins.states.bluetooth

import android.content.Context
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBluetoothPlugin : BluetoothPlugin {
    override val id: String = "ksensor.states.bluetooth"
    override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)

    private val context: Context by lazy { KSensorContext.get() }

    override fun connections(): StatePlugin<StateData.BleConnectionStatus> = object : StatePlugin<StateData.BleConnectionStatus> {
        override val id: String = "${this@AndroidBluetoothPlugin.id}.connections"
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: StateData.BleConnectionStatus
            get() = StateData.BleConnectionStatus(emptyList()) // Placeholder

        override fun observe(): Flow<StateData.BleConnectionStatus> = callbackFlow {
            val receiver = BleConnectionReceiver(context) { trySend(it) }
            receiver.register()
            awaitClose { receiver.unregister() }
        }
    }

    override fun discoveries(): StatePlugin<StateData.BleDiscoversStatus> = object : StatePlugin<StateData.BleDiscoversStatus> {
        override val id: String = "${this@AndroidBluetoothPlugin.id}.discoveries"
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: StateData.BleDiscoversStatus = StateData.BleDiscoversStatus(emptyList())
        
        override fun observe(): Flow<StateData.BleDiscoversStatus> = callbackFlow {
            val receiver = BleDiscoversReceiver(context, { trySend(it) }, { close(it) })
            receiver.register()
            awaitClose { receiver.unregister() }
        }
    }
}

actual fun createBluetoothPlugin(): BluetoothPlugin = AndroidBluetoothPlugin()
