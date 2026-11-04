package com.ksensor.plugins.sensors.environment.proximity

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceProximityStateDidChangeNotification

class IosProximityPlugin : ProximityPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> = callbackFlow {
        val device = UIDevice.currentDevice
        device.proximityMonitoringEnabled = true
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIDeviceProximityStateDidChangeNotification,
            `object` = device,
            queue = NSOperationQueue.mainQueue,
            usingBlock = {
                val isNear = device.proximityState
                val sensorData = SensorData.Proximity(if (isNear) 0f else -1f, isNear)
                trySend(KSensorResponse(sensorData))
            }
        )
        awaitClose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            device.proximityMonitoringEnabled = false
        }
    }
}

actual fun createProximityPlugin(): ProximityPlugin = IosProximityPlugin()
