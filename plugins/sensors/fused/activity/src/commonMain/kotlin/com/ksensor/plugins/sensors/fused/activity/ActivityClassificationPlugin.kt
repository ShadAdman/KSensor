package com.ksensor.plugins.sensors.fused.activity

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
import com.ksensor.core.model.SensorData.FusedActivityType
import com.ksensor.core.trackPluginDiagnostics
import com.ksensor.plugins.sensors.motion.accelerometer.AccelerometerPlugin
import com.ksensor.plugins.sensors.motion.accelerometer.createAccelerometerPlugin
import com.ksensor.plugins.sensors.motion.gyroscope.GyroscopePlugin
import com.ksensor.plugins.sensors.motion.gyroscope.createGyroscopePlugin
import com.ksensor.plugins.sensors.motion.motiondetector.MotionDetectorPlugin
import com.ksensor.plugins.sensors.motion.motiondetector.createMotionDetectorPlugin
import com.ksensor.plugins.sensors.motion.stepcounter.StepCounterPlugin
import com.ksensor.plugins.sensors.motion.stepcounter.createStepCounterPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.sqrt

/**
 * Higher-level fused plugin for Motion Activity Classification.
 * Combines raw data from Motion Detector, Accelerometer, Gyroscope, and Step Counter
 * into a single output with dynamic confidence scoring and source breakdown.
 */
interface ActivityClassificationPlugin : SensorPlugin<SensorData.FusedActivity> {
    override val id: PluginId get() = PluginId.FUSED_ACTIVITY
    override val requiredPermissions: List<Permission> get() = listOf(Permission.ACTIVITY_RECOGNITION)

    fun activity(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedActivity>> = observe(config)
}

class DefaultActivityClassificationPlugin(
    private val accelerometerPlugin: AccelerometerPlugin = createAccelerometerPlugin(),
    private val gyroscopePlugin: GyroscopePlugin = createGyroscopePlugin(),
    private val stepCounterPlugin: StepCounterPlugin = createStepCounterPlugin(),
    private val motionDetectorPlugin: MotionDetectorPlugin = createMotionDetectorPlugin()
) : ActivityClassificationPlugin {

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedActivity>> {
        val accelFlow: Flow<KSensorResponse<SensorData.Accelerometer>?> = accelerometerPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Accelerometer(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Accelerometer>, KSensorResponse<SensorData.Accelerometer>?> { it }
            .onStart { emit(null) }

        val gyroFlow: Flow<KSensorResponse<SensorData.Gyroscope>?> = gyroscopePlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Gyroscope(com.ksensor.core.model.Vector3(0f, 0f, 0f)))) }
            .map<KSensorResponse<SensorData.Gyroscope>, KSensorResponse<SensorData.Gyroscope>?> { it }
            .onStart { emit(null) }

        val stepFlow: Flow<KSensorResponse<SensorData.StepCounter>?> = stepCounterPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.StepCounter(0))) }
            .map<KSensorResponse<SensorData.StepCounter>, KSensorResponse<SensorData.StepCounter>?> { it }
            .onStart { emit(null) }

        val motionFlow: Flow<KSensorResponse<SensorData.MotionDetector>?> = motionDetectorPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.MotionDetector(SensorData.MotionType.STATIONARY))) }
            .map<KSensorResponse<SensorData.MotionDetector>, KSensorResponse<SensorData.MotionDetector>?> { it }
            .onStart { emit(null) }

        return combine(accelFlow, gyroFlow, stepFlow, motionFlow) { accel, gyro, step, motion ->
            val now = currentTimestamp()
            val sources = mutableListOf<FusedSourceInfo>()

            var totalWeight = 0f
            var activeWeight = 0f

            // 1. Motion Detector Source (weight: 0.45)
            val motionWeight = 0.45f
            totalWeight += motionWeight
            val isMotionAvailable = motionDetectorPlugin.isSupported && motion != null
            if (isMotionAvailable) activeWeight += motionWeight
            sources.add(FusedSourceInfo(PluginId.MOTION_DETECTOR, isMotionAvailable, motionWeight, now))

            // 2. Accelerometer Source (weight: 0.30)
            val accelWeight = 0.30f
            totalWeight += accelWeight
            val isAccelAvailable = accelerometerPlugin.isSupported && accel != null
            if (isAccelAvailable) activeWeight += accelWeight
            sources.add(FusedSourceInfo(PluginId.ACCELEROMETER, isAccelAvailable, accelWeight, now))

            // 3. Gyroscope Source (weight: 0.15)
            val gyroWeight = 0.15f
            totalWeight += gyroWeight
            val isGyroAvailable = gyroscopePlugin.isSupported && gyro != null
            if (isGyroAvailable) activeWeight += gyroWeight
            sources.add(FusedSourceInfo(PluginId.GYROSCOPE, isGyroAvailable, gyroWeight, now))

            // 4. Step Counter Source (weight: 0.10)
            val stepWeight = 0.10f
            totalWeight += stepWeight
            val isStepAvailable = stepCounterPlugin.isSupported && step != null
            if (isStepAvailable) activeWeight += stepWeight
            sources.add(FusedSourceInfo(PluginId.STEP_COUNTER, isStepAvailable, stepWeight, now))

            val confidenceScore = if (totalWeight > 0f) activeWeight / totalWeight else 0f

            // Classify Activity
            val classifiedActivity = when {
                motion != null -> when (motion.data.type) {
                    SensorData.MotionType.WALKING -> FusedActivityType.WALKING
                    SensorData.MotionType.RUNNING -> FusedActivityType.RUNNING
                    SensorData.MotionType.CYCLING -> FusedActivityType.CYCLING
                    SensorData.MotionType.AUTOMOTIVE -> FusedActivityType.AUTOMOTIVE
                    SensorData.MotionType.STATIONARY -> FusedActivityType.STATIONARY
                    SensorData.MotionType.UNKNOWN -> FusedActivityType.UNKNOWN
                }
                accel != null -> {
                    val mag = sqrt(
                        accel.data.values.x * accel.data.values.x +
                        accel.data.values.y * accel.data.values.y +
                        accel.data.values.z * accel.data.values.z
                    )
                    val dev = kotlin.math.abs(mag - 9.81f)
                    when {
                        dev > 8.0f -> FusedActivityType.RUNNING
                        dev > 2.5f -> FusedActivityType.WALKING
                        else -> FusedActivityType.STATIONARY
                    }
                }
                else -> FusedActivityType.UNKNOWN
            }

            val quality = if (activeWeight > 0.5f) 0.95f else 0.5f
            val explanation = "Fused from ${sources.count { it.isAvailable }}/${sources.size} active motion sensors"

            KSensorResponse(
                data = SensorData.FusedActivity(
                    result = classifiedActivity,
                    confidence = FusedConfidence(
                        score = confidenceScore,
                        level = ConfidenceLevel.fromScore(confidenceScore),
                        quality = quality,
                        explanation = explanation
                    ),
                    sources = sources
                )
            )
        }.trackPluginDiagnostics(id)
    }
}

expect fun createActivityClassificationPlugin(): ActivityClassificationPlugin
