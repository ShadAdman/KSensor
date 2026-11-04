package com.ksensor.plugins.sensors.positioning

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import com.ksensor.plugins.sensors.positioning.heading.HeadingPlugin
import com.ksensor.plugins.sensors.positioning.heading.createHeadingPlugin
import com.ksensor.plugins.sensors.positioning.location.LocationPlugin
import com.ksensor.plugins.sensors.positioning.location.createLocationPlugin
import com.ksensor.plugins.sensors.positioning.locationstatus.LocationStatusPlugin
import com.ksensor.plugins.sensors.positioning.locationstatus.createLocationStatusPlugin
import com.ksensor.plugins.sensors.positioning.magnetometer.MagnetometerPlugin
import com.ksensor.plugins.sensors.positioning.magnetometer.createMagnetometerPlugin
import com.ksensor.plugins.sensors.positioning.orientation.OrientationPlugin
import com.ksensor.plugins.sensors.positioning.orientation.createOrientationPlugin
import kotlinx.coroutines.flow.Flow

interface PositioningPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.POSITIONING
    override val requiredPermissions: List<Permission> get() = listOf(Permission.LOCATION)

    fun location(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Location>>
    fun magnetometer(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Magnetometer>>
    fun orientation(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Orientation>>
    fun heading(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.Heading>>
    fun locationStatus(): StatePlugin<StateData.LocationStatus>
}

class DefaultPositioningPlugin(
    private val locationPlugin: LocationPlugin = createLocationPlugin(),
    private val magnetometerPlugin: MagnetometerPlugin = createMagnetometerPlugin(),
    private val orientationPlugin: OrientationPlugin = createOrientationPlugin(),
    private val headingPlugin: HeadingPlugin = createHeadingPlugin(),
    private val locationStatusPlugin: LocationStatusPlugin = createLocationStatusPlugin()
) : PositioningPlugin {
    override fun location(config: SensorConfig): Flow<KSensorResponse<SensorData.Location>> =
        locationPlugin.observe(config)

    override fun magnetometer(config: SensorConfig): Flow<KSensorResponse<SensorData.Magnetometer>> =
        magnetometerPlugin.observe(config)

    override fun orientation(config: SensorConfig): Flow<KSensorResponse<SensorData.Orientation>> =
        orientationPlugin.observe(config)

    override fun heading(config: SensorConfig): Flow<KSensorResponse<SensorData.Heading>> =
        headingPlugin.observe(config)

    override fun locationStatus(): StatePlugin<StateData.LocationStatus> = locationStatusPlugin
}

fun createPositioningPlugin(): PositioningPlugin = DefaultPositioningPlugin()
