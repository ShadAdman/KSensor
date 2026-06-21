package com.ksensor.plugins.states.bluetooth

import com.ksensor.core.model.BleDevice
import com.ksensor.core.model.StateData
import platform.CoreBluetooth.*
import platform.darwin.NSObject

internal class BleDiscoversReceiver(
    private val onData: (StateData.BleDiscoversStatus) -> Unit,
    private val onError: (Exception) -> Unit
) : NSObject(), CBCentralManagerDelegateProtocol {

    private var centralManager: CBCentralManager? = null
    private val discoveredDevices = mutableMapOf<String, BleDevice>()

    fun register() {
        centralManager = CBCentralManager(delegate = this, queue = null)
    }

    fun unregister() {
        centralManager?.stopScan()
        centralManager = null
        discoveredDevices.clear()
    }

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        if (central.state == CBManagerStatePoweredOn) {
            central.scanForPeripheralsWithServices(null, null)
        } else {
            onError(Exception("Bluetooth is not powered on (state: ${central.state})"))
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: platform.Foundation.NSNumber
    ) {
        val bleDevice = BleDevice(
            id = didDiscoverPeripheral.identifier.UUIDString,
            name = didDiscoverPeripheral.name ?: "Unknown Device"
        )
        discoveredDevices[bleDevice.id] = bleDevice
        emitCurrentState()
    }

    private fun emitCurrentState() {
        onData(StateData.BleDiscoversStatus(discoveredDevices.values.toList()))
    }
}
