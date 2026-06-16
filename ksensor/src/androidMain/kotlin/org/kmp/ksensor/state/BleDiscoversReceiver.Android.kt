package org.kmp.ksensor.state

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context

internal class BleDiscoversReceiver(
    private val context: Context,
    private val onData: (StateUpdate) -> Unit
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private val discoveredDevices = mutableMapOf<String, BleDevice>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val bleDevice = BleDevice(
                id = device.address,
                name = device.name ?: "Unknown Device"
            )
            discoveredDevices[bleDevice.id] = bleDevice
            emitCurrentState()
        }

        override fun onScanFailed(errorCode: Int) {
            onData(StateUpdate.Error(Exception("BLE Scan failed with error code: $errorCode")))
        }
    }

    @SuppressLint("MissingPermission")
    fun register() {
        if (bluetoothAdapter?.isEnabled == true) {
            bleScanner?.startScan(scanCallback)
        } else {
            onData(StateUpdate.Error(Exception("Bluetooth is disabled")))
        }
    }

    @SuppressLint("MissingPermission")
    fun unregister() {
        try {
            bleScanner?.stopScan(scanCallback)
        } catch (_: Exception) {
        }
    }

    private fun emitCurrentState() {
        onData(
            StateUpdate.Data(
                type = StateType.BLE_DISCOVERS,
                data = StateData.BleDiscoversStatus(discoveredDevices.values.toList()),
                platformType = PlatformType.Android
            )
        )
    }
}