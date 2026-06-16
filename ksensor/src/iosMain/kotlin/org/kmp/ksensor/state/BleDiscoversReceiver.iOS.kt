package org.kmp.ksensor.state

import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.darwin.NSObject

internal class BleDiscoversReceiver(
    private val onData: (StateUpdate) -> Unit,
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
            onData(StateUpdate.Error(Exception("Bluetooth is not powered on (state: ${central.state})")))
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
        onData(
            StateUpdate.Data(
                type = StateType.BLE_DISCOVERS,
                data = StateData.BleDiscoversStatus(discoveredDevices.values.toList()),
                platformType = PlatformType.iOS
            )
        )
    }
}