package com.ksensor.plugins.states.system.storage

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class IosStorageStatusPlugin(private val defaultInterval: Duration = 3.seconds) : StorageStatusPlugin {
    override val currentState: KSensorResponse<StateData.StorageStatus>
        get() = KSensorResponse(StorageProvider.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.StorageStatus>> = flow {
        while (true) {
            emit(KSensorResponse(StorageProvider.getCurrentStatus()))
            delay(defaultInterval)
        }
    }
}

actual fun createStorageStatusPlugin(interval: Duration): StorageStatusPlugin = IosStorageStatusPlugin(interval)
