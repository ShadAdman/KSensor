package com.ksensor.plugins.sensors.positioning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

internal class LocationProviderReceiver(private val onProviderChanged: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.MODE_CHANGED_ACTION) {
            onProviderChanged()
        }
    }
}
