package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.Permission
import com.ksensor.core.model.PluginId
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlin.concurrent.Volatile

class IosInteractionPlugin : InteractionPlugin {
    override val id: PluginId = PluginId.INTERACTION
    override val requiredPermissions: List<Permission> = emptyList()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun touchGestures(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> = callbackFlow {
        TouchGesturesMonitor.registerObserver {
            trySend(KSensorResponse(it))
        }
        awaitClose { TouchGesturesMonitor.removeObserver() }
    }

    @Volatile
    private var lastScrollStatus = StateData.ScrollStatus(0f, 0f, isScrolling = false)

    private val scrollFlow by lazy {
        callbackFlow {
            ScrollMonitor.registerObserver { trySend(it) }
            awaitClose { ScrollMonitor.removeObserver() }
        }.withIdleDetection()
            .onEach { lastScrollStatus = it }
            .map { KSensorResponse(it) }
            .shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    private val scrollPlugin = object : StatePlugin<StateData.ScrollStatus> {
        override val id: PluginId = PluginId.INTERACTION
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.ScrollStatus>
            get() = KSensorResponse(lastScrollStatus)

        override fun observe(): Flow<KSensorResponse<StateData.ScrollStatus>> = scrollFlow
    }

    override fun scroll(): StatePlugin<StateData.ScrollStatus> = scrollPlugin
}

actual fun createInteractionPlugin(): InteractionPlugin = IosInteractionPlugin()
