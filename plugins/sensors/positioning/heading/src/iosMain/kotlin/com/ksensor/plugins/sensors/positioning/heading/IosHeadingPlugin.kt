package com.ksensor.plugins.sensors.positioning.heading

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

class IosHeadingPlugin : HeadingPlugin {
    @OptIn(ExperimentalForeignApi::class)
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Heading>> = callbackFlow {
        val headingManager = CLLocationManager()
        var lastMagneticHeading = 0.0
        var lastTrueHeading = 0.0
        var lastDeviceHeading = 0.0
        var lastCourseOverGround = 0.0

        fun sendHeading() {
            trySend(KSensorResponse(SensorData.Heading(
                magneticHeading = lastMagneticHeading,
                trueHeading = lastTrueHeading,
                deviceHeading = lastDeviceHeading,
                courseOverGround = lastCourseOverGround
            )))
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
                lastMagneticHeading = didUpdateHeading.magneticHeading
                lastTrueHeading = didUpdateHeading.trueHeading
                lastDeviceHeading = didUpdateHeading.magneticHeading
                sendHeading()
            }

            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation
                loc?.let {
                    if (it.course >= 0) {
                        lastCourseOverGround = it.course
                        sendHeading()
                    }
                }
            }
        }

        headingManager.delegate = delegate
        headingManager.startUpdatingLocation()
        headingManager.startUpdatingHeading()

        awaitClose {
            headingManager.stopUpdatingLocation()
            headingManager.stopUpdatingHeading()
        }
    }
}

actual fun createHeadingPlugin(): HeadingPlugin = IosHeadingPlugin()
