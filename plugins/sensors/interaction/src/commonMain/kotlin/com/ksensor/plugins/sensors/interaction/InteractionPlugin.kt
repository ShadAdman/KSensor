package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow

interface InteractionPlugin : KSensorPlugin {
    fun touchGestures(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<SensorData.TouchGestures>>

    /**
     * Live scroll position (horizontal and vertical) of the most recently scrolled container.
     */
    fun scroll(): StatePlugin<StateData.ScrollStatus>
}

expect fun createInteractionPlugin(): InteractionPlugin
