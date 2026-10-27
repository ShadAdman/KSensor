package com.ksensor.plugins.states.system.brightness

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmBrightnessStatusPlugin : BrightnessStatusPlugin {
    override val currentState: KSensorResponse<StateData.BrightnessStatus>
        get() = KSensorResponse(StateData.BrightnessStatus(100))

    override fun observe(): Flow<KSensorResponse<StateData.BrightnessStatus>> = emptyFlow()
}

actual fun createBrightnessStatusPlugin(): BrightnessStatusPlugin = JvmBrightnessStatusPlugin()
