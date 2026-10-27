package com.ksensor.plugins.sensors.positioning.heading

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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

class AndroidHeadingPlugin : HeadingPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Heading>>>()

    @SuppressLint("MissingPermission")
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Heading>> =
        flows.getOrPut(config) {
            callbackFlow {
                var lastLocation: Location? = null
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

                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lastLocation = location
                        if (location.hasBearing()) {
                            lastCourseOverGround = location.bearing.toDouble()
                        }
                        lastLocation?.let {
                            val geomagField = GeomagneticField(
                                it.latitude.toFloat(),
                                it.longitude.toFloat(),
                                it.altitude.toFloat(),
                                it.time
                            )
                            lastTrueHeading = (lastMagneticHeading + geomagField.declination + 360) % 360
                        }
                        sendHeading()
                    }
                }

                val sensorListener = object : SensorEventListener {
                    private val rotationMatrix = FloatArray(9)
                    private val orientationValues = FloatArray(3)

                    override fun onSensorChanged(event: SensorEvent) {
                        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            SensorManager.getOrientation(rotationMatrix, orientationValues)

                            val azimuth = Math.toDegrees(orientationValues[0].toDouble())
                            lastMagneticHeading = (azimuth + 360) % 360
                            lastDeviceHeading = lastMagneticHeading

                            lastLocation?.let {
                                val geomagField = GeomagneticField(
                                    it.latitude.toFloat(),
                                    it.longitude.toFloat(),
                                    it.altitude.toFloat(),
                                    it.time
                                )
                                lastTrueHeading = (lastMagneticHeading + geomagField.declination + 360) % 360
                            } ?: run {
                                lastTrueHeading = lastMagneticHeading
                            }
                            sendHeading()
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        config.intervalMs,
                        0f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                } catch (e: SecurityException) {
                    // Ignore or log
                }

                val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                if (rotationSensor != null) {
                    sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }

                awaitClose {
                    locationManager.removeUpdates(locationListener)
                    sensorManager.unregisterListener(sensorListener)
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createHeadingPlugin(): HeadingPlugin = AndroidHeadingPlugin()
