package com.ksensor.plugins.sensors.positioning.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidLocationPlugin : LocationPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val locationFlows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Location>>>()

    @SuppressLint("MissingPermission")
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Location>> =
        locationFlows.getOrPut(config) {
            callbackFlow {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val data = SensorData.Location(location.latitude, location.longitude, location.altitude)
                        trySend(KSensorResponse(data))
                    }
                }
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        config.intervalMs,
                        1f,
                        listener,
                        Looper.getMainLooper()
                    )
                } catch (e: SecurityException) {
                    close(e)
                }
                awaitClose { locationManager.removeUpdates(listener) }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createLocationPlugin(): LocationPlugin = AndroidLocationPlugin()
