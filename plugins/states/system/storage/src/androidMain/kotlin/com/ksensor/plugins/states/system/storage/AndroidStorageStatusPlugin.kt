package com.ksensor.plugins.states.system.storage

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

class AndroidStorageStatusPlugin(private val defaultInterval: Duration = 3.seconds) : StorageStatusPlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val storageFlows = ConcurrentHashMap<Duration, Flow<KSensorResponse<StateData.StorageStatus>>>()

    override val currentState: KSensorResponse<StateData.StorageStatus>
        get() = KSensorResponse(StorageProvider.getCurrentStatus())

    override fun observe(): Flow<KSensorResponse<StateData.StorageStatus>> =
        storageFlows.getOrPut(defaultInterval) {
            flow {
                while (true) {
                    emit(KSensorResponse(StorageProvider.getCurrentStatus()))
                    delay(defaultInterval)
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createStorageStatusPlugin(interval: Duration): StorageStatusPlugin = AndroidStorageStatusPlugin(interval)
