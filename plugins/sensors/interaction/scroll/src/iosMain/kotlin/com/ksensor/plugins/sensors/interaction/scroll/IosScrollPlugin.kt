package com.ksensor.plugins.sensors.interaction.scroll

import com.ksensor.core.model.KSensorResponse
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

class IosScrollPlugin : ScrollPlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var lastScrollStatus = StateData.ScrollStatus(0f, 0f, isScrolling = false)

    override val currentState: KSensorResponse<StateData.ScrollStatus>
        get() = KSensorResponse(lastScrollStatus)

    private val scrollFlow by lazy {
        callbackFlow {
            ScrollMonitor.registerObserver { trySend(it) }
            awaitClose { ScrollMonitor.removeObserver() }
        }.withIdleDetection()
            .onEach { lastScrollStatus = it }
            .map { KSensorResponse(it) }
            .shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.ScrollStatus>> = scrollFlow
}

actual fun createScrollPlugin(): ScrollPlugin = IosScrollPlugin()
