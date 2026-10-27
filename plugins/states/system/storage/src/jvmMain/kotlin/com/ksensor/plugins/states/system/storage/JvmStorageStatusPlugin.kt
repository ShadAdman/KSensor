package com.ksensor.plugins.states.system.storage

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class JvmStorageStatusPlugin(private val interval: Duration = 3.seconds) : StorageStatusPlugin {
    override val currentState: KSensorResponse<StateData.StorageStatus>
        get() = KSensorResponse(StateData.StorageStatus(0L, 0L, 0L))

    override fun observe(): Flow<KSensorResponse<StateData.StorageStatus>> = emptyFlow()
}

actual fun createStorageStatusPlugin(interval: Duration): StorageStatusPlugin = JvmStorageStatusPlugin(interval)
