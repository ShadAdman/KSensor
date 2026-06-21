package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.Permission
import com.ksensor.core.PlatformType
import com.ksensor.core.PluginId
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosInteractionPlugin : InteractionPlugin {
    override val id: PluginId = PluginId.INTERACTION
    override val requiredPermissions: List<Permission> = emptyList()

    override fun touchGestures(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> = callbackFlow {
        TouchGesturesMonitor.registerObserver {
            trySend(KSensorResponse(it, PlatformType.iOS))
        }
        awaitClose { TouchGesturesMonitor.removeObserver() }
    }
}

actual fun createInteractionPlugin(): InteractionPlugin = IosInteractionPlugin()
