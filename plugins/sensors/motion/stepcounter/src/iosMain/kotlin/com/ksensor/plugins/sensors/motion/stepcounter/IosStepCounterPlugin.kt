package com.ksensor.plugins.sensors.motion.stepcounter

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMPedometer
import platform.Foundation.NSDate

class IosStepCounterPlugin : StepCounterPlugin {
    private val pedometer = if (CMPedometer.isStepCountingAvailable()) CMPedometer() else null

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.StepCounter>> = callbackFlow {
        if (pedometer == null) {
            close()
            return@callbackFlow
        }

        pedometer.startPedometerUpdatesFromDate(NSDate()) { data, _ ->
            data?.let {
                val sensorData = SensorData.StepCounter(it.numberOfSteps.intValue)
                trySend(KSensorResponse(sensorData))
            }
        }

        awaitClose { pedometer.stopPedometerUpdates() }
    }
}

actual fun createStepCounterPlugin(): StepCounterPlugin = IosStepCounterPlugin()
