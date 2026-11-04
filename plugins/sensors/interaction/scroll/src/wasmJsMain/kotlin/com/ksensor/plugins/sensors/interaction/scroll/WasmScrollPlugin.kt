package com.ksensor.plugins.sensors.interaction.scroll

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmScrollPlugin : ScrollPlugin {
    override val currentState: KSensorResponse<StateData.ScrollStatus>
        get() = KSensorResponse(StateData.ScrollStatus(0f, 0f, false))
    override fun observe(): Flow<KSensorResponse<StateData.ScrollStatus>> = emptyFlow()
}

actual fun createScrollPlugin(): ScrollPlugin = WasmScrollPlugin()
