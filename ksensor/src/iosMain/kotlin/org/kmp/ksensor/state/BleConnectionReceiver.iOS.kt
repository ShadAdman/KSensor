package org.kmp.ksensor.state

import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBPeripheral
import platform.darwin.NSObject

internal class BleConnectionReceiver(
    private val onData: (StateUpdate) -> Unit,
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

        onData(
            StateUpdate.Data(
                type = StateType.BLE_CONNECTIONS,
                data = StateData.BleConnectionStatus(devices),
                platformType = PlatformType.iOS
            )
        )
    }
}
