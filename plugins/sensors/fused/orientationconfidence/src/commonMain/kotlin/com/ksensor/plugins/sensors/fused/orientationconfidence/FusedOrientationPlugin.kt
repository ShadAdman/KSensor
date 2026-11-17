package com.ksensor.plugins.sensors.fused.orientationconfidence

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.currentTimestamp
import com.ksensor.core.model.ConfidenceLevel
import com.ksensor.core.model.DeviceOrientation
import com.ksensor.core.model.FusedConfidence
import com.ksensor.core.model.FusedSourceInfo
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import com.ksensor.plugins.sensors.motion.accelerometer.AccelerometerPlugin
import com.ksensor.plugins.sensors.motion.accelerometer.createAccelerometerPlugin
import com.ksensor.plugins.sensors.motion.gyroscope.GyroscopePlugin
import com.ksensor.plugins.sensors.motion.gyroscope.createGyroscopePlugin
import com.ksensor.plugins.sensors.positioning.magnetometer.MagnetometerPlugin
import com.ksensor.plugins.sensors.positioning.magnetometer.createMagnetometerPlugin
import com.ksensor.plugins.sensors.positioning.orientation.OrientationPlugin
import com.ksensor.plugins.sensors.positioning.orientation.createOrientationPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Higher-level fused plugin for Fused Device Orientation Confidence.
 * Combines Accelerometer, Gyroscope, Magnetometer, and System Orientation
 * to provide 3D spatial rotation angles (pitch, roll, yaw), orientation classification,
 * stability indicators, and source confidence.
 */
interface FusedOrientationPlugin : SensorPlugin<SensorData.FusedOrientation> {
    override val id: PluginId get() = PluginId.FUSED_ORIENTATION
    override val requiredPermissions: List<Permission> get() = emptyList()

    fun fusedOrientation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedOrientation>> = observe(config)
}

class DefaultFusedOrientationPlugin(
    private val accelerometerPlugin: AccelerometerPlugin = createAccelerometerPlugin(),
    private val gyroscopePlugin: GyroscopePlugin = createGyroscopePlugin(),
    private val magnetometerPlugin: MagnetometerPlugin = createMagnetometerPlugin(),
    private val orientationPlugin: OrientationPlugin = createOrientationPlugin()
) : FusedOrientationPlugin {

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedOrientation>> {
        val accelFlow: Flow<KSensorResponse<SensorData.Accelerometer>?> = accelerometerPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Accelerometer(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Accelerometer>, KSensorResponse<SensorData.Accelerometer>?> { it }
            .onStart { emit(null) }

        val gyroFlow: Flow<KSensorResponse<SensorData.Gyroscope>?> = gyroscopePlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Gyroscope(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Gyroscope>, KSensorResponse<SensorData.Gyroscope>?> { it }
            .onStart { emit(null) }

        val magFlow: Flow<KSensorResponse<SensorData.Magnetometer>?> = magnetometerPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Magnetometer(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Magnetometer>, KSensorResponse<SensorData.Magnetometer>?> { it }
            .onStart { emit(null) }

        val orientFlow: Flow<KSensorResponse<SensorData.Orientation>?> = orientationPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Orientation(DeviceOrientation.PORTRAIT))) }
            .map<KSensorResponse<SensorData.Orientation>, KSensorResponse<SensorData.Orientation>?> { it }
            .onStart { emit(null) }

        return combine(accelFlow, gyroFlow, magFlow, orientFlow) { accel, gyro, mag, orient ->
            val now = currentTimestamp()
            val sources = mutableListOf<FusedSourceInfo>()

            var totalWeight = 0f
            var activeWeight = 0f

            val accelW = 0.35f
            totalWeight += accelW
            val isAccelAvail = accelerometerPlugin.isSupported && accel != null
            if (isAccelAvail) activeWeight += accelW
            sources.add(FusedSourceInfo(PluginId.ACCELEROMETER, isAccelAvail, accelW, now))

            val gyroW = 0.30f
            totalWeight += gyroW
            val isGyroAvail = gyroscopePlugin.isSupported && gyro != null
            if (isGyroAvail) activeWeight += gyroW
            sources.add(FusedSourceInfo(PluginId.GYROSCOPE, isGyroAvail, gyroW, now))

            val magW = 0.20f
            totalWeight += magW
            val isMagAvail = magnetometerPlugin.isSupported && mag != null
            if (isMagAvail) activeWeight += magW
            sources.add(FusedSourceInfo(PluginId.MAGNETOMETER, isMagAvail, magW, now))

            val orientW = 0.15f
            totalWeight += orientW
            val isOrientAvail = orientationPlugin.isSupported && orient != null
            if (isOrientAvail) activeWeight += orientW
            sources.add(FusedSourceInfo(PluginId.ORIENTATION, isOrientAvail, orientW, now))

            val score = if (totalWeight > 0f) activeWeight / totalWeight else 0f

            val pitch = if (accel != null) {
                (atan2(accel.data.values.y, sqrt(accel.data.values.x * accel.data.values.x + accel.data.values.z * accel.data.values.z)) * 180f / kotlin.math.PI).toFloat()
            } else 0f

            val roll = if (accel != null) {
                (atan2(-accel.data.values.x, accel.data.values.z) * 180f / kotlin.math.PI).toFloat()
            } else 0f

            val yaw = if (mag != null) {
                (atan2(mag.data.values.y, mag.data.values.x) * 180f / kotlin.math.PI).toFloat()
            } else 0f

            val isStable = if (gyro != null) {
                val rotMag = sqrt(gyro.data.values.x * gyro.data.values.x + gyro.data.values.y * gyro.data.values.y + gyro.data.values.z * gyro.data.values.z)
                rotMag < 0.15f
            } else true

            val derivedOrientation = when {
                orient != null -> orient.data.orientation
                kotlin.math.abs(pitch) > 45f -> DeviceOrientation.PORTRAIT
                kotlin.math.abs(roll) > 45f -> DeviceOrientation.LANDSCAPE
                else -> DeviceOrientation.PORTRAIT
            }

            val explanation = "3D Fused orientation calculated from ${sources.count { it.isAvailable }}/${sources.size} active sensors"

            KSensorResponse(
                data = SensorData.FusedOrientation(
                    orientation = derivedOrientation,
                    pitchDegrees = pitch,
                    rollDegrees = roll,
                    yawDegrees = yaw,
                    isStable = isStable,
                    confidence = FusedConfidence(
                        score = score,
                        level = ConfidenceLevel.fromScore(score),
                        quality = if (isStable) 0.95f else 0.70f,
                        explanation = explanation
                    ),
                    sources = sources
                )
            )
        }.trackPluginDiagnostics(id)
    }
}

expect fun createFusedOrientationPlugin(): FusedOrientationPlugin
