package com.ksensor.plugins.sensors.positioning.orientation

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.DeviceOrientation
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification

class IosOrientationPlugin : OrientationPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Orientation>> = callbackFlow {
        UIDevice.currentDevice.beginGeneratingDeviceOrientationNotifications()
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIDeviceOrientationDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) {
            val orientation = UIDevice.currentDevice.orientation
            val mapped = when (orientation) {
                UIDeviceOrientation.UIDeviceOrientationPortrait,
                UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> DeviceOrientation.PORTRAIT
                UIDeviceOrientation.UIDeviceOrientationLandscapeLeft,
                UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> DeviceOrientation.LANDSCAPE
                else -> DeviceOrientation.UNKNOWN
            }
            val sensorData = SensorData.Orientation(mapped, orientation.value.toInt())
            trySend(KSensorResponse(sensorData))
        }
        awaitClose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
        }
    }
}

actual fun createOrientationPlugin(): OrientationPlugin = IosOrientationPlugin()
