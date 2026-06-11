package org.kmp.ksensor.state

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class BleConnectionReceiver(
    private val context: Context,
    private val onData: (StateUpdate) -> Unit
) : BroadcastReceiver() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    fun register() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(this, filter)
        emitCurrentState()
    }

    fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (_: Exception) {
            // Already unregistered or not registered
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        emitCurrentState()
    }

    @SuppressLint("MissingPermission")
    fun emitCurrentState() {
        try {
            val connectedDevices = if (bluetoothAdapter?.isEnabled == true) {
                bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).map { device ->
                    StateData.BleConnectionStatus.BleDevice(
                        id = device.address,
                        name = device.name ?: "Unknown Device"
                    )
                }
            } else {
                emptyList()
            }

            onData(
                StateUpdate.Data(
                    type = StateType.BLE_CONNECTION,
                    data = StateData.BleConnectionStatus(connectedDevices),
                    platformType = PlatformType.Android
                )
            )
        } catch (e: Exception) {
            onData(StateUpdate.Error(e))
        }
    }
}
