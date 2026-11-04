package com.ksensor.plugins.states.bluetooth.audiodevices

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import com.ksensor.plugins.states.bluetooth.connections.BleConnectionsStatusPlugin
import com.ksensor.plugins.states.bluetooth.connections.createBleConnectionsStatusPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BleAudioDevicesStatusPlugin : StatePlugin<StateData.BleConnectionStatus> {
    override val id: PluginId get() = PluginId.BLE_AUDIO_DEVICES
    override val requiredPermissions: List<Permission> get() = listOf(Permission.BLUETOOTH)
}

class DefaultBleAudioDevicesStatusPlugin(
    private val connectionsPlugin: BleConnectionsStatusPlugin = createBleConnectionsStatusPlugin()
) : BleAudioDevicesStatusPlugin {
    override val currentState: KSensorResponse<StateData.BleConnectionStatus>
        get() = KSensorResponse(StateData.BleConnectionStatus(connectionsPlugin.currentState.data.connectedDevices.filter { it.isAudio }))

    override fun observe(): Flow<KSensorResponse<StateData.BleConnectionStatus>> =
        connectionsPlugin.observe().map { response ->
            KSensorResponse(StateData.BleConnectionStatus(response.data.connectedDevices.filter { it.isAudio }))
        }
}

fun createBleAudioDevicesStatusPlugin(): BleAudioDevicesStatusPlugin = DefaultBleAudioDevicesStatusPlugin()
