package com.ksensor.plugins.states.bluetooth

import android.content.Context
import com.ksensor.core.Permission
import com.ksensor.core.PlatformType
import com.ksensor.core.PluginId
import com.ksensor.core.StatePlugin
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBluetoothPlugin : BluetoothPlugin {
    override val id: PluginId = PluginId.BLUETOOTH
    override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)

    private val context: Context by lazy { KSensorContext.get() }

    override fun connections(): StatePlugin<StateData.BleConnectionStatus> = object : StatePlugin<StateData.BleConnectionStatus> {
        override val id: PluginId = PluginId.BLUETOOTH
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: KSensorResponse<StateData.BleConnectionStatus>
            get() = KSensorResponse(StateData.BleConnectionStatus(emptyList()), PlatformType.Android) // Placeholder

        override fun observe(): Flow<KSensorResponse<StateData.BleConnectionStatus>> = callbackFlow {
            val receiver = BleConnectionReceiver(context) { trySend(KSensorResponse(it, PlatformType.Android)) }
            receiver.register()
            awaitClose { receiver.unregister() }
        }
    }

    override fun discoveries(): StatePlugin<StateData.BleDiscoversStatus> = object : StatePlugin<StateData.BleDiscoversStatus> {
        override val id: PluginId = PluginId.BLUETOOTH
        override val requiredPermissions: List<Permission> = listOf(Permission.BLUETOOTH)
        override val currentState: KSensorResponse<StateData.BleDiscoversStatus> = KSensorResponse(StateData.BleDiscoversStatus(emptyList()), PlatformType.Android)
        
        override fun observe(): Flow<KSensorResponse<StateData.BleDiscoversStatus>> = callbackFlow {
            val receiver = BleDiscoversReceiver(context, { trySend(KSensorResponse(it, PlatformType.Android)) }, { close(it) })
            receiver.register()
            awaitClose { receiver.unregister() }
        }
    }
}

actual fun createBluetoothPlugin(): BluetoothPlugin = AndroidBluetoothPlugin()
