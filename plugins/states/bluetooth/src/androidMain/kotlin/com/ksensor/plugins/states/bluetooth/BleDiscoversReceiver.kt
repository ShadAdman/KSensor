package com.ksensor.plugins.states.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.ksensor.core.model.BleDevice
import com.ksensor.core.model.StateData

internal class BleDiscoversReceiver(
    private val context: Context,
    private val onData: (StateData.BleDiscoversStatus) -> Unit,
    private val onError: (Exception) -> Unit
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
            onError(Exception("BLE Scan failed with error code: $errorCode"))
        }
    }

    @SuppressLint("MissingPermission")
    fun register() {
        if (bluetoothAdapter?.isEnabled == true) {
            bleScanner?.startScan(scanCallback)
        } else {
            onError(Exception("Bluetooth is disabled"))
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
        onData(StateData.BleDiscoversStatus(discoveredDevices.values.toList()))
    }
}
