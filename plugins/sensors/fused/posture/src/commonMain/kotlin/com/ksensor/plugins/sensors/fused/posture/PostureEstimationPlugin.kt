package com.ksensor.plugins.sensors.fused.posture

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.SensorPlugin
import com.ksensor.core.currentTimestamp
import com.ksensor.core.model.ConfidenceLevel
import com.ksensor.core.model.FusedConfidence
import com.ksensor.core.model.FusedSourceInfo
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.SensorData.FusedPostureType
import com.ksensor.core.trackPluginDiagnostics
import com.ksensor.plugins.sensors.environment.proximity.ProximityPlugin
import com.ksensor.plugins.sensors.environment.proximity.createProximityPlugin
import com.ksensor.plugins.sensors.motion.accelerometer.AccelerometerPlugin
import com.ksensor.plugins.sensors.motion.accelerometer.createAccelerometerPlugin
import com.ksensor.plugins.sensors.motion.gyroscope.GyroscopePlugin
import com.ksensor.plugins.sensors.motion.gyroscope.createGyroscopePlugin
import com.ksensor.plugins.sensors.positioning.orientation.OrientationPlugin
import com.ksensor.plugins.sensors.positioning.orientation.createOrientationPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.abs

/**
 * Higher-level fused plugin for Device Posture Estimation.
 * Combines Accelerometer, Gyroscope, Proximity, and Orientation sensor streams
 * to estimate device posture (in hand, at ear, in pocket, lying down, sitting, standing).
 */
interface PostureEstimationPlugin : SensorPlugin<SensorData.FusedPosture> {
    override val id: PluginId get() = PluginId.FUSED_POSTURE
    override val requiredPermissions: List<Permission> get() = emptyList()

    fun posture(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedPosture>> = observe(config)
}

class DefaultPostureEstimationPlugin(
    private val accelerometerPlugin: AccelerometerPlugin = createAccelerometerPlugin(),
    private val gyroscopePlugin: GyroscopePlugin = createGyroscopePlugin(),
    private val proximityPlugin: ProximityPlugin = createProximityPlugin(),
    private val orientationPlugin: OrientationPlugin = createOrientationPlugin()
) : PostureEstimationPlugin {

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedPosture>> {
        val accelFlow: Flow<KSensorResponse<SensorData.Accelerometer>?> = accelerometerPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Accelerometer(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Accelerometer>, KSensorResponse<SensorData.Accelerometer>?> { it }
            .onStart { emit(null) }

        val gyroFlow: Flow<KSensorResponse<SensorData.Gyroscope>?> = gyroscopePlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Gyroscope(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Gyroscope>, KSensorResponse<SensorData.Gyroscope>?> { it }
            .onStart { emit(null) }

        val proxFlow: Flow<KSensorResponse<SensorData.Proximity>?> = proximityPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Proximity(10f, false))) }
            .map<KSensorResponse<SensorData.Proximity>, KSensorResponse<SensorData.Proximity>?> { it }
            .onStart { emit(null) }

        val orientFlow: Flow<KSensorResponse<SensorData.Orientation>?> = orientationPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Orientation(com.ksensor.core.model.DeviceOrientation.PORTRAIT))) }
            .map<KSensorResponse<SensorData.Orientation>, KSensorResponse<SensorData.Orientation>?> { it }
            .onStart { emit(null) }

        return combine(accelFlow, gyroFlow, proxFlow, orientFlow) { accel, gyro, prox, orient ->
            val now = currentTimestamp()
            val sources = mutableListOf<FusedSourceInfo>()

            var totalWeight = 0f
            var activeWeight = 0f

            val accelW = 0.35f
            totalWeight += accelW
            val isAccelAvail = accelerometerPlugin.isSupported && accel != null
            if (isAccelAvail) activeWeight += accelW
            sources.add(FusedSourceInfo(PluginId.ACCELEROMETER, isAccelAvail, accelW, now))

            val proxW = 0.30f
            totalWeight += proxW
            val isProxAvail = proximityPlugin.isSupported && prox != null
            if (isProxAvail) activeWeight += proxW
            sources.add(FusedSourceInfo(PluginId.PROXIMITY, isProxAvail, proxW, now))

            val orientW = 0.20f
            totalWeight += orientW
            val isOrientAvail = orientationPlugin.isSupported && orient != null
            if (isOrientAvail) activeWeight += orientW
            sources.add(FusedSourceInfo(PluginId.ORIENTATION, isOrientAvail, orientW, now))

            val gyroW = 0.15f
            totalWeight += gyroW
            val isGyroAvail = gyroscopePlugin.isSupported && gyro != null
            if (isGyroAvail) activeWeight += gyroW
            sources.add(FusedSourceInfo(PluginId.GYROSCOPE, isGyroAvail, gyroW, now))

            val score = if (totalWeight > 0f) activeWeight / totalWeight else 0f

            val estimatedPosture = when {
                prox != null && prox.data.isNear -> {
                    if (orient?.data?.orientation == com.ksensor.core.model.DeviceOrientation.PORTRAIT) {
                        FusedPostureType.AT_EAR
                    } else {
                        FusedPostureType.IN_POCKET
                    }
                }
                accel != null -> {
                    val z = accel.data.values.z
                    val y = accel.data.values.y
                    val absZ = abs(z)
                    when {
                        absZ > 8.5f -> FusedPostureType.LYING_DOWN
                        y > 6.0f -> FusedPostureType.STANDING
                        y > 2.0f -> FusedPostureType.IN_HAND
                        else -> FusedPostureType.SITTING
                    }
                }
                orient != null -> FusedPostureType.IN_HAND
                else -> FusedPostureType.UNKNOWN
            }

            val explanation = "Posture calculated from ${sources.count { it.isAvailable }}/${sources.size} active sensors"

            KSensorResponse(
                data = SensorData.FusedPosture(
                    result = estimatedPosture,
                    confidence = FusedConfidence(
                        score = score,
                        level = ConfidenceLevel.fromScore(score),
                        quality = if (score > 0.5f) 0.9f else 0.5f,
                        explanation = explanation
                    ),
                    sources = sources
                )
            )
        }.trackPluginDiagnostics(id)
    }
}

expect fun createPostureEstimationPlugin(): PostureEstimationPlugin
