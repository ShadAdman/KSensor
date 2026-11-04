package com.ksensor.plugins.states.system.resources

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class IosResourcesStatusPlugin(private val defaultInterval: Duration = 3.seconds) : ResourcesStatusPlugin {
    override val currentState: KSensorResponse<StateData.ResourcesStatus>
        get() = KSensorResponse(ResourcesProvider.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.ResourcesStatus>> = flow {
        while (true) {
            emit(KSensorResponse(ResourcesProvider.getCurrentStatus()))
            delay(defaultInterval)
        }
    }
}

actual fun createResourcesStatusPlugin(interval: Duration): ResourcesStatusPlugin = IosResourcesStatusPlugin(interval)
