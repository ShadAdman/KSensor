package com.ksensor.plugins.sensors.motion

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
import com.ksensor.core.model.Vector3
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidMotionPlugin : MotionPlugin {
    override val id: PluginId = PluginId.MOTION
    override val requiredPermissions: List<Permission> = emptyList()

    private val sensorManager: SensorManager by lazy {
        KSensorContext.get().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun accelerometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Accelerometer>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val maximumRange = sensor.maximumRange
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val data = SensorData.Accelerometer(
                    Vector3(
                        event.values[0] / maximumRange,
                        event.values[1] / maximumRange,
                        event.values[2] / maximumRange
                    )
                )
                trySend(KSensorResponse(data))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun gyroscope(config: SensorConfig): Flow<KSensorResponse<SensorData.Gyroscope>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val data = SensorData.Gyroscope(
                    Vector3(event.values[0], event.values[1], event.values[2])
                )
                trySend(KSensorResponse(data))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun stepCounter(config: SensorConfig): Flow<KSensorResponse<SensorData.StepCounter>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val data = SensorData.StepCounter(event.values[0].toInt())
                trySend(KSensorResponse(data))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun stepDetector(config: SensorConfig): Flow<KSensorResponse<SensorData.StepDetector>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(KSensorResponse(SensorData.StepDetector))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

actual fun createMotionPlugin(): MotionPlugin = AndroidMotionPlugin()
