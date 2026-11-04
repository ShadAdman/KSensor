package com.ksensor.plugins.sensors.positioning.location

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

class IosLocationPlugin : LocationPlugin {
    private val locationManager = CLLocationManager()

    @OptIn(ExperimentalForeignApi::class)
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Location>> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation
                loc?.let {
                    it.coordinate.useContents {
                        val sensorData = SensorData.Location(latitude, longitude, it.altitude)
                        trySend(KSensorResponse(sensorData))
                    }
                }
            }
            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {}
        }
        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
        awaitClose { locationManager.stopUpdatingLocation() }
    }
}

actual fun createLocationPlugin(): LocationPlugin = IosLocationPlugin()
