package com.ksensor.plugins.sensors.positioning

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.DeviceOrientation
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.darwin.NSObject

class IosPositioningPlugin : PositioningPlugin {
    override val id: String = "ksensor.sensors.positioning"
    override val requiredPermissions: List<Permission> = listOf(Permission.LOCATION)

    private val locationManager = CLLocationManager()
    private val motionManager = CMMotionManager()

    @OptIn(ExperimentalForeignApi::class)
    override fun location(config: SensorConfig): Flow<SensorData.Location> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation
                loc?.let {
                    it.coordinate.useContents {
                        trySend(SensorData.Location(latitude, longitude, it.altitude))
                    }
                }
            }
            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                // Handle error
            }
        }
        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
        awaitClose { locationManager.stopUpdatingLocation() }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun magnetometer(config: SensorConfig): Flow<SensorData.Magnetometer> = callbackFlow {
        if (!motionManager.magnetometerAvailable) {
            close()
            return@callbackFlow
        }
        motionManager.magnetometerUpdateInterval = config.samplingIntervalMs / 1000.0
        motionManager.startMagnetometerUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.magneticField?.useContents {
                trySend(SensorData.Magnetometer(Vector3(x.toFloat(), y.toFloat(), z.toFloat())))
            }
        }
        awaitClose { motionManager.stopMagnetometerUpdates() }
    }

    override fun orientation(config: SensorConfig): Flow<SensorData.Orientation> = callbackFlow {
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
            trySend(SensorData.Orientation(mapped, orientation.toInt()))
        }
        awaitClose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
        }
    }
}

actual fun createPositioningPlugin(): PositioningPlugin = IosPositioningPlugin()
