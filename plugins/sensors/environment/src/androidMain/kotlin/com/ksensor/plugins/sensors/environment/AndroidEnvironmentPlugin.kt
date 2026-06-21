package com.ksensor.plugins.sensors.environment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ksensor.core.Permission
import com.ksensor.core.PluginId
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidEnvironmentPlugin : EnvironmentPlugin {
    override val id: PluginId = PluginId.ENVIRONMENT
    override val requiredPermissions: List<Permission> = emptyList()

    private val sensorManager: SensorManager by lazy {
        KSensorContext.get().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun barometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(KSensorResponse(SensorData.Barometer(event.values[0])))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun light(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(KSensorResponse(SensorData.LightIlluminance(event.values[0])))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun proximity(config: SensorConfig): Flow<KSensorResponse<SensorData.Proximity>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val distance = event.values[0]
                trySend(KSensorResponse(SensorData.Proximity(distance, distance < sensor.maximumRange)))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

actual fun createEnvironmentPlugin(): EnvironmentPlugin = AndroidEnvironmentPlugin()
