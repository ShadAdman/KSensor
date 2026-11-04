package com.ksensor.plugins.states.system.resources

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class JvmResourcesStatusPlugin(private val interval: Duration = 3.seconds) : ResourcesStatusPlugin {
    override val currentState: KSensorResponse<StateData.ResourcesStatus>
        get() = KSensorResponse(
            StateData.ResourcesStatus(
                cpuUsagePercent = 0.0,
                appCpuUsagePercent = 0.0,
                systemLoadAverage = emptyList(),
                memoryPressure = StateData.ResourcesStatus.MemoryPressureLevel.UNKNOWN,
                memoryUsage = StateData.ResourcesStatus.MemoryUsage(0L, 0L, 0L)
            )
        )

    override fun observe(): Flow<KSensorResponse<StateData.ResourcesStatus>> = emptyFlow()
}

actual fun createResourcesStatusPlugin(interval: Duration): ResourcesStatusPlugin = JvmResourcesStatusPlugin(interval)
