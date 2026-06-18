package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow

interface InteractionPlugin : KSensorPlugin {
    fun touchGestures(config: SensorConfig = SensorConfig.Default): Flow<SensorData.TouchGestures>
}

expect fun createInteractionPlugin(): InteractionPlugin
