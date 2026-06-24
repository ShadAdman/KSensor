package com.ksensor.plugins.states.bluetooth

import com.ksensor.core.model.BleDevice
import com.ksensor.core.model.StateData
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBPeripheral
import platform.darwin.NSObject

internal class BleConnectionReceiver(
    private val onData: (StateData.BleConnectionStatus) -> Unit
) : NSObject(), CBCentralManagerDelegateProtocol {

    private var centralManager: CBCentralManager? = null
    private val connectedPeripherals = mutableSetOf<CBPeripheral>()

    fun register() {
        centralManager = CBCentralManager(delegate = this, queue = null)
    }

    fun unregister() {
        centralManager = null
        connectedPeripherals.clear()
    }

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        emitCurrentState()
    }

    override fun centralManager(central: CBCentralManager, didConnectPeripheral: CBPeripheral) {
        connectedPeripherals.add(didConnectPeripheral)
        emitCurrentState()
    }

    override fun centralManager(central: CBCentralManager, didDisconnectPeripheral: CBPeripheral, error: platform.Foundation.NSError?) {
        connectedPeripherals.remove(didDisconnectPeripheral)
        emitCurrentState()
    }

    private fun emitCurrentState() {
        val devices = connectedPeripherals.map { peripheral ->
            BleDevice(
                id = peripheral.identifier.UUIDString,
                name = peripheral.name ?: "Unknown Device"
            )
        }
        onData(StateData.BleConnectionStatus(devices))
    }
}
