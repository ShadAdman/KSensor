package com.ksensor.plugins.states.system.resources

import android.content.Context
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AndroidResourcesStatusPlugin(private val defaultInterval: Duration = 3.seconds) : ResourcesStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val resourcesFlows = ConcurrentHashMap<Duration, Flow<KSensorResponse<StateData.ResourcesStatus>>>()

    override val currentState: KSensorResponse<StateData.ResourcesStatus>
        get() = KSensorResponse(ResourcesProvider.getCurrentStatus(context))

    override fun observe(): Flow<KSensorResponse<StateData.ResourcesStatus>> =
        resourcesFlows.getOrPut(defaultInterval) {
            flow {
                while (true) {
                    emit(KSensorResponse(ResourcesProvider.getCurrentStatus(context)))
                    delay(defaultInterval)
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createResourcesStatusPlugin(interval: Duration): ResourcesStatusPlugin = AndroidResourcesStatusPlugin(interval)
