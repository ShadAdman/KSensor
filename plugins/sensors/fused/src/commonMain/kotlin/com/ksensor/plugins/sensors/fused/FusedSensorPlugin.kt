package com.ksensor.plugins.sensors.fused

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.fused.activity.ActivityClassificationPlugin
import com.ksensor.plugins.sensors.fused.activity.createActivityClassificationPlugin
import com.ksensor.plugins.sensors.fused.posture.PostureEstimationPlugin
import com.ksensor.plugins.sensors.fused.posture.createPostureEstimationPlugin
import com.ksensor.plugins.sensors.fused.surrounding.SurroundingPlugin
import com.ksensor.plugins.sensors.fused.surrounding.createSurroundingPlugin
import com.ksensor.plugins.sensors.fused.threedorientation.Fused3DOrientationPlugin
import com.ksensor.plugins.sensors.fused.threedorientation.createFused3DOrientationPlugin
import kotlinx.coroutines.flow.Flow

/**
 * Parent group plugin for Fused Sensor capabilities.
 * Combines Activity Classification, Posture Estimation, Fused 3D Orientation, and Device Surroundings.
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
    fun fused3DOrientation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Fused3DOrientation>>
    fun fusedSurrounding(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.FusedSurrounding>>
}

class DefaultFusedSensorPlugin(
    private val activityPlugin: ActivityClassificationPlugin = createActivityClassificationPlugin(),
    private val posturePlugin: PostureEstimationPlugin = createPostureEstimationPlugin(),
    private val orientationPlugin: Fused3DOrientationPlugin = createFused3DOrientationPlugin(),
    private val surroundingPlugin: SurroundingPlugin = createSurroundingPlugin()
) : FusedSensorPlugin {

    override fun activityClassification(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedActivity>> =
        activityPlugin.observe(config)

    override fun postureEstimation(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedPosture>> =
        posturePlugin.observe(config)

    override fun fused3DOrientation(config: SensorConfig): Flow<KSensorResponse<SensorData.Fused3DOrientation>> =
        orientationPlugin.observe(config)

    override fun fusedSurrounding(config: SensorConfig): Flow<KSensorResponse<SensorData.FusedSurrounding>> =
        surroundingPlugin.observe(config)
}

fun createFusedSensorPlugin(): FusedSensorPlugin = DefaultFusedSensorPlugin()
