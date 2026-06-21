package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidInteractionPlugin : InteractionPlugin {
    override val id: String = "ksensor.sensors.interaction"
    override val requiredPermissions: List<Permission> = emptyList()

    override fun touchGestures(config: SensorConfig): Flow<SensorData.TouchGestures> = callbackFlow {
        TouchGesturesMonitor.registerObserver { trySend(it) }
        awaitClose { TouchGesturesMonitor.removeObserver() }
    }
}

actual fun createInteractionPlugin(): InteractionPlugin = AndroidInteractionPlugin()
