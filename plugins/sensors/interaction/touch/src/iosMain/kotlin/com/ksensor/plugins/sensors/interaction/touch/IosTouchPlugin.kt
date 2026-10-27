package com.ksensor.plugins.sensors.interaction.touch

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosTouchPlugin : TouchPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.TouchGestures>> = callbackFlow {
        TouchGesturesMonitor.registerObserver {
            trySend(KSensorResponse(it))
        }
        awaitClose { TouchGesturesMonitor.removeObserver() }
    }
}

actual fun createTouchPlugin(): TouchPlugin = IosTouchPlugin()
