package com.ksensor.plugins.states.system

import com.ksensor.core.model.StateData
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryLevelDidChangeNotification
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIDeviceBatteryStateDidChangeNotification

internal class BatteryStateReceiver(private val onData: (StateData.BatteryStatus) -> Unit) {
    private val device = UIDevice.currentDevice
    private var batteryLevelObserver: platform.darwin.NSObjectProtocol? = null
    private var batteryStateObserver: platform.darwin.NSObjectProtocol? = null

    private fun emitData() {
        val levelRaw = device.batteryLevel
        val percent: Int? = if (levelRaw < 0f) null else (levelRaw * 100f).toInt()
        val state = when (device.batteryState) {
            UIDeviceBatteryState.UIDeviceBatteryStateCharging -> StateData.BatteryStatus.ChargingState.CHARGING
            UIDeviceBatteryState.UIDeviceBatteryStateFull -> StateData.BatteryStatus.ChargingState.FULL
            UIDeviceBatteryState.UIDeviceBatteryStateUnplugged -> StateData.BatteryStatus.ChargingState.DISCHARGING
            else -> StateData.BatteryStatus.ChargingState.UNKNOWN
        }
        onData(
            StateData.BatteryStatus(
                levelPercent = percent,
                chargingState = state,
                health = null,
                temperatureC = null
            )
        )
    }

    fun register() {
        device.batteryMonitoringEnabled = true
        val center = NSNotificationCenter.defaultCenter
        batteryLevelObserver = center.addObserverForName(
            name = UIDeviceBatteryLevelDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ -> emitData() }

        batteryStateObserver = center.addObserverForName(
            name = UIDeviceBatteryStateDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ -> emitData() }

        emitData()
    }

    fun unregister() {
        batteryLevelObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        batteryStateObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        device.batteryMonitoringEnabled = false
    }

    fun getCurrentStatus(): StateData.BatteryStatus {
        val levelRaw = device.batteryLevel
        val percent: Int? = if (levelRaw < 0f) null else (levelRaw * 100f).toInt()
        val state = when (device.batteryState) {
            UIDeviceBatteryState.UIDeviceBatteryStateCharging -> StateData.BatteryStatus.ChargingState.CHARGING
            UIDeviceBatteryState.UIDeviceBatteryStateFull -> StateData.BatteryStatus.ChargingState.FULL
            UIDeviceBatteryState.UIDeviceBatteryStateUnplugged -> StateData.BatteryStatus.ChargingState.DISCHARGING
            else -> StateData.BatteryStatus.ChargingState.UNKNOWN
        }
        return StateData.BatteryStatus(percent, state, null, null)
    }
}
