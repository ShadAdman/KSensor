package com.ksensor.plugins.sensors.positioning.locationstatus

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationStatusPlugin : LocationStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override val currentState: KSensorResponse<StateData.LocationStatus>
        get() = KSensorResponse(StateData.LocationStatus(locationManager.isLocationEnabled))

    override fun observe(): Flow<KSensorResponse<StateData.LocationStatus>> = callbackFlow {
        val receiver = LocationProviderReceiver {
            trySend(KSensorResponse(StateData.LocationStatus(locationManager.isLocationEnabled)))
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}

actual fun createLocationStatusPlugin(): LocationStatusPlugin = AndroidLocationStatusPlugin()
