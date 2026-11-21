package com.ksensor.plugins.sensors.fused.context

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
import com.ksensor.core.model.SensorData.FusedEnvironmentState
import com.ksensor.core.model.SensorData.FusedMotionState
import com.ksensor.core.trackPluginDiagnostics
import com.ksensor.plugins.sensors.environment.barometer.BarometerPlugin
import com.ksensor.plugins.sensors.environment.barometer.createBarometerPlugin
import com.ksensor.plugins.sensors.environment.light.LightPlugin
import com.ksensor.plugins.sensors.environment.light.createLightPlugin
import com.ksensor.plugins.sensors.environment.noise.NoisePlugin
import com.ksensor.plugins.sensors.environment.noise.createNoisePlugin
import com.ksensor.plugins.sensors.motion.motiondetector.MotionDetectorPlugin
import com.ksensor.plugins.sensors.motion.motiondetector.createMotionDetectorPlugin
import com.ksensor.plugins.sensors.positioning.location.LocationPlugin
import com.ksensor.plugins.sensors.positioning.location.createLocationPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Higher-level fused plugin for Environmental & Context Summary.
 * Combines Motion Detector, Location, Light, Barometer, and Noise sensors
 * into a unified context summary output with confidence scoring and contributing source details.
 */
interface ContextSummaryPlugin : SensorPlugin<SensorData.FusedContextSummary> {
    override val id: PluginId get() = PluginId.FUSED_CONTEXT
    override val requiredPermissions: List<Permission>
        get() = listOf(Permission.LOCATION, Permission.RECORD_AUDIO)

    fun contextSummary(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedContextSummary>> = observe(config)
}

class DefaultContextSummaryPlugin(
    private val motionDetectorPlugin: MotionDetectorPlugin = createMotionDetectorPlugin(),
    private val locationPlugin: LocationPlugin = createLocationPlugin(),
    private val lightPlugin: LightPlugin = createLightPlugin(),
    private val barometerPlugin: BarometerPlugin = createBarometerPlugin(),
    private val noisePlugin: NoisePlugin = createNoisePlugin()
) : ContextSummaryPlugin {

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedContextSummary>> {
        val motionFlow: Flow<KSensorResponse<SensorData.MotionDetector>?> = motionDetectorPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.MotionDetector(SensorData.MotionType.STATIONARY))) }
            .map<KSensorResponse<SensorData.MotionDetector>, KSensorResponse<SensorData.MotionDetector>?> { it }
            .onStart { emit(null) }

        val locFlow: Flow<KSensorResponse<SensorData.Location>?> = locationPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Location(0.0, 0.0, 0.0))) }
            .map<KSensorResponse<SensorData.Location>, KSensorResponse<SensorData.Location>?> { it }
            .onStart { emit(null) }

        val lightFlow: Flow<KSensorResponse<SensorData.LightIlluminance>?> = lightPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.LightIlluminance(150f))) }
            .map<KSensorResponse<SensorData.LightIlluminance>, KSensorResponse<SensorData.LightIlluminance>?> { it }
            .onStart { emit(null) }

        val baroFlow: Flow<KSensorResponse<SensorData.Barometer>?> = barometerPlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Barometer(1013.25f))) }
            .map<KSensorResponse<SensorData.Barometer>, KSensorResponse<SensorData.Barometer>?> { it }
            .onStart { emit(null) }

        val noiseFlow: Flow<KSensorResponse<SensorData.Noise>?> = noisePlugin.observe(config)
            .catch { emit(KSensorResponse(SensorData.Noise(45f, false, currentTimestamp()))) }
            .map<KSensorResponse<SensorData.Noise>, KSensorResponse<SensorData.Noise>?> { it }
            .onStart { emit(null) }

        return combine(motionFlow, locFlow, lightFlow, baroFlow, noiseFlow) { motion, loc, light, baro, noise ->
            val now = currentTimestamp()
            val sources = mutableListOf<FusedSourceInfo>()

            var totalWeight = 0f
            var activeWeight = 0f

            val motionW = 0.30f
            totalWeight += motionW
            val isMotionAvail = motionDetectorPlugin.isSupported && motion != null
            if (isMotionAvail) activeWeight += motionW
            sources.add(FusedSourceInfo(PluginId.MOTION_DETECTOR, isMotionAvail, motionW, now))

            val lightW = 0.25f
            totalWeight += lightW
            val isLightAvail = lightPlugin.isSupported && light != null
            if (isLightAvail) activeWeight += lightW
            sources.add(FusedSourceInfo(PluginId.LIGHT, isLightAvail, lightW, now))

            val baroW = 0.20f
            totalWeight += baroW
            val isBaroAvail = barometerPlugin.isSupported && baro != null
            if (isBaroAvail) activeWeight += baroW
            sources.add(FusedSourceInfo(PluginId.BAROMETER, isBaroAvail, baroW, now))

            val noiseW = 0.15f
            totalWeight += noiseW
            val isNoiseAvail = noisePlugin.isSupported && noise != null
            if (isNoiseAvail) activeWeight += noiseW
            sources.add(FusedSourceInfo(PluginId.NOISE, isNoiseAvail, noiseW, now))

            val locW = 0.10f
            totalWeight += locW
            val isLocAvail = locationPlugin.isSupported && loc != null
            if (isLocAvail) activeWeight += locW
            sources.add(FusedSourceInfo(PluginId.LOCATION, isLocAvail, locW, now))

            val score = if (totalWeight > 0f) activeWeight / totalWeight else 0f

            val motionState = when (motion?.data?.type) {
                SensorData.MotionType.STATIONARY -> FusedMotionState.STILL
                SensorData.MotionType.WALKING, SensorData.MotionType.RUNNING -> FusedMotionState.IN_MOTION
                SensorData.MotionType.AUTOMOTIVE, SensorData.MotionType.CYCLING -> FusedMotionState.HIGH_SPEED
                else -> FusedMotionState.UNKNOWN
            }

            val lux = light?.data?.illuminance ?: 0f
            val pressure = baro?.data?.pressure ?: 1013.25f

            val envState = when {
                motion?.data?.type == SensorData.MotionType.AUTOMOTIVE -> FusedEnvironmentState.IN_VEHICLE
                pressure < 950f -> FusedEnvironmentState.ELEVATED
                lux >= 300f -> FusedEnvironmentState.OUTDOOR
                lux < 300f && lux > 0f -> FusedEnvironmentState.INDOOR
                else -> FusedEnvironmentState.UNKNOWN
            }

            val isIndoorEst = envState == FusedEnvironmentState.INDOOR || lux < 300f

            val dB = noise?.data?.dB ?: 0f
            val noiseSummary = when {
                dB >= 75f -> "LOUD (${dB.toInt()} dB)"
                dB >= 50f -> "MODERATE (${dB.toInt()} dB)"
                dB > 0f -> "QUIET (${dB.toInt()} dB)"
                else -> "UNKNOWN"
            }

            val explanation = "Context summary fused from ${sources.count { it.isAvailable }}/${sources.size} active environmental & location sensors"

            KSensorResponse(
                data = SensorData.FusedContextSummary(
                    environmentState = envState,
                    motionState = motionState,
                    isIndoorEstimate = isIndoorEst,
                    noiseLevelSummary = noiseSummary,
                    confidence = FusedConfidence(
                        score = score,
                        level = ConfidenceLevel.fromScore(score),
                        quality = if (score > 0.6f) 0.90f else 0.50f,
                        explanation = explanation
                    ),
                    sources = sources
                )
            )
        }.trackPluginDiagnostics(id)
    }
}

expect fun createContextSummaryPlugin(): ContextSummaryPlugin
