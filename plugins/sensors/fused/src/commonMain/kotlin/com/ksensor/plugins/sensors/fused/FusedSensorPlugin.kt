package com.ksensor.plugins.sensors.fused

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.fused.activity.ActivityClassificationPlugin
import com.ksensor.plugins.sensors.fused.activity.createActivityClassificationPlugin
import com.ksensor.plugins.sensors.fused.context.ContextSummaryPlugin
import com.ksensor.plugins.sensors.fused.context.createContextSummaryPlugin
import com.ksensor.plugins.sensors.fused.orientationconfidence.FusedOrientationPlugin
import com.ksensor.plugins.sensors.fused.orientationconfidence.createFusedOrientationPlugin
import com.ksensor.plugins.sensors.fused.posture.PostureEstimationPlugin
import com.ksensor.plugins.sensors.fused.posture.createPostureEstimationPlugin
import kotlinx.coroutines.flow.Flow

/**
 * Parent group plugin for Fused Sensor capabilities.
 * Combines Activity Classification, Posture Estimation, Fused Orientation, and Context Summary.
 */
interface FusedSensorPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.FUSED_SENSOR
    override val requiredPermissions: List<Permission>
        get() = listOf(
            Permission.ACTIVITY_RECOGNITION,
            Permission.LOCATION,
            Permission.RECORD_AUDIO
        )

    fun activityClassification(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedActivity>>
    fun postureEstimation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedPosture>>
    fun fusedOrientation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedOrientation>>
    fun contextSummary(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedContextSummary>>
}

class DefaultFusedSensorPlugin(
    private val activityPlugin: ActivityClassificationPlugin = createActivityClassificationPlugin(),
    private val posturePlugin: PostureEstimationPlugin = createPostureEstimationPlugin(),
    private val orientationPlugin: FusedOrientationPlugin = createFusedOrientationPlugin(),
    private val contextPlugin: ContextSummaryPlugin = createContextSummaryPlugin()
) : FusedSensorPlugin {

    override fun activityClassification(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedActivity>> =
        activityPlugin.observe(config)

    override fun postureEstimation(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedPosture>> =
        posturePlugin.observe(config)

    override fun fusedOrientation(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedOrientation>> =
        orientationPlugin.observe(config)

    override fun contextSummary(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedContextSummary>> =
        contextPlugin.observe(config)
}

fun createFusedSensorPlugin(): FusedSensorPlugin = DefaultFusedSensorPlugin()
