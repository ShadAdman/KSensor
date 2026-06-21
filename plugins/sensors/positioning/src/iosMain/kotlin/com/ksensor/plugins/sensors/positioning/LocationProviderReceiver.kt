package com.ksensor.plugins.sensors.positioning

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.darwin.NSObject

internal class LocationProviderReceiver(private val isLocationOn: (Boolean) -> Unit) : NSObject(), CLLocationManagerDelegateProtocol {

    private val locationManager = CLLocationManager()

    init {
        locationManager.delegate = this
        isLocationOn(isLocationCurrentlyEnabled())

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null,
            usingBlock = {
                isLocationOn(isLocationCurrentlyEnabled())
            }
        )
    }

    private fun isLocationCurrentlyEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled() &&
                CLLocationManager.authorizationStatus() != kCLAuthorizationStatusDenied &&
                CLLocationManager.authorizationStatus() != kCLAuthorizationStatusRestricted
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        isLocationOn(isLocationCurrentlyEnabled())
    }

    fun dispose() {
        locationManager.delegate = null
    }
}
