package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.Permission
import com.ksensor.core.model.PluginId
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * No-op InteractionPlugin for the web (Wasm) target. KSensor has no web (Wasm) sensor
 * backend yet, so observations emit nothing and state values are placeholders.
 */
class WasmInteractionPlugin : InteractionPlugin {
    override val id: PluginId = PluginId.INTERACTION
    override val requiredPermissions: List<Permission> = emptyList()

    override fun touchGestures(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> = emptyFlow()

    override fun scroll(): StatePlugin<StateData.ScrollStatus> = object : StatePlugin<StateData.ScrollStatus> {
        override val id: PluginId = PluginId.INTERACTION
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.ScrollStatus> =
            KSensorResponse(StateData.ScrollStatus(0f, 0f, isScrolling = false))
        override fun observe(): Flow<KSensorResponse<StateData.ScrollStatus>> = emptyFlow()
    }
}

actual fun createInteractionPlugin(): InteractionPlugin = WasmInteractionPlugin()
