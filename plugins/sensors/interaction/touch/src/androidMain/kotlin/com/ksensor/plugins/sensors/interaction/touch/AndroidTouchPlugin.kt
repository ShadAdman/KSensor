package com.ksensor.plugins.sensors.interaction.touch

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidTouchPlugin : TouchPlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.TouchGestures>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> =
        flows.getOrPut(config) {
            callbackFlow {
                TouchGesturesMonitor.registerObserver {
                    trySend(KSensorResponse(it))
                }
                awaitClose { TouchGesturesMonitor.removeObserver() }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createTouchPlugin(): TouchPlugin = AndroidTouchPlugin()
