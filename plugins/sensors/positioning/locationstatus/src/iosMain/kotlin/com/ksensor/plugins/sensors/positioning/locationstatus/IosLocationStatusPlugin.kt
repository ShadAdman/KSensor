package com.ksensor.plugins.sensors.positioning.locationstatus

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted

class IosLocationStatusPlugin : LocationStatusPlugin {
    override val currentState: KSensorResponse<StateData.LocationStatus>
        get() = KSensorResponse(StateData.LocationStatus(isLocationCurrentlyEnabled()))

    override fun observe(): Flow<KSensorResponse<StateData.LocationStatus>> = callbackFlow {
        val receiver = LocationProviderReceiver {
            trySend(KSensorResponse(StateData.LocationStatus(it)))
        }
        awaitClose { receiver.dispose() }
    }

    private fun isLocationCurrentlyEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled() &&
                CLLocationManager.authorizationStatus() != kCLAuthorizationStatusDenied &&
                CLLocationManager.authorizationStatus() != kCLAuthorizationStatusRestricted
    }
}

actual fun createLocationStatusPlugin(): LocationStatusPlugin = IosLocationStatusPlugin()
