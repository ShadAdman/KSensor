package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import com.ksensor.plugins.sensors.interaction.scroll.ScrollPlugin
import com.ksensor.plugins.sensors.interaction.scroll.createScrollPlugin
import com.ksensor.plugins.sensors.interaction.touch.TouchPlugin
import com.ksensor.plugins.sensors.interaction.touch.createTouchPlugin
import kotlinx.coroutines.flow.Flow

interface InteractionPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.INTERACTION
    override val requiredPermissions: List<Permission> get() = emptyList()

    fun touchGestures(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.TouchGestures>>
    fun scroll(): StatePlugin<StateData.ScrollStatus>
}

class DefaultInteractionPlugin(
    private val touchPlugin: TouchPlugin = createTouchPlugin(),
    private val scrollPlugin: ScrollPlugin = createScrollPlugin()
) : InteractionPlugin {
    override fun touchGestures(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> =
        touchPlugin.observe(config)

    override fun scroll(): StatePlugin<StateData.ScrollStatus> = scrollPlugin
}

fun createInteractionPlugin(): InteractionPlugin = DefaultInteractionPlugin()
