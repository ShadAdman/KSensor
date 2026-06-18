package com.ksensor.plugins.sensors.positioning

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.view.OrientationEventListener
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.DeviceOrientation
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidPositioningPlugin : PositioningPlugin {
    override val id: String = "ksensor.sensors.positioning"
    override val requiredPermissions: List<Permission> = listOf(Permission.LOCATION)

    private val context: Context by lazy { KSensorContext.get() }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    override fun location(config: SensorConfig): Flow<SensorData.Location> = callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(SensorData.Location(location.latitude, location.longitude, location.altitude))
            }
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                config.samplingIntervalMs,
                1f,
                listener
            )
        } catch (e: SecurityException) {
            close(e)
        }
        awaitClose { locationManager.removeUpdates(listener) }
    }

    override fun magnetometer(config: SensorConfig): Flow<SensorData.Magnetometer> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(SensorData.Magnetometer(Vector3(event.values[0], event.values[1], event.values[2])))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun orientation(config: SensorConfig): Flow<SensorData.Orientation> = callbackFlow {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val mapped = when (orientation) {
                    in 45..134 -> DeviceOrientation.LANDSCAPE
                    in 135..224 -> DeviceOrientation.PORTRAIT
                    in 225..314 -> DeviceOrientation.LANDSCAPE
                    in 315..360, in 0..44 -> DeviceOrientation.PORTRAIT
                    else -> DeviceOrientation.UNKNOWN
                }
                trySend(SensorData.Orientation(mapped, orientation))
            }
        }
        listener.enable()
        awaitClose { listener.disable() }
    }
}

actual fun createPositioningPlugin(): PositioningPlugin = AndroidPositioningPlugin()
