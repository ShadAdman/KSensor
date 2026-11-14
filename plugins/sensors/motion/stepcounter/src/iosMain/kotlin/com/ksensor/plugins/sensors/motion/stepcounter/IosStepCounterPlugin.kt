package com.ksensor.plugins.sensors.motion.stepcounter

import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMPedometer
import platform.Foundation.NSDate

class IosStepCounterPlugin : StepCounterPlugin {
    override val isSupported: Boolean get() = CMPedometer.isStepCountingAvailable()
    override val unsupportedReason: String? get() = if (!isSupported) "Pedometer step counting is not supported on this iOS device" else null

    private val pedometer = if (CMPedometer.isStepCountingAvailable()) CMPedometer() else null

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.StepCounter>> = callbackFlow {
        if (pedometer == null) {
            KSensor.reportStatus(
                pluginId = id,
                status = PluginStatus.UNSUPPORTED,
                unsupportedReason = unsupportedReason
            )
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
    }.trackPluginDiagnostics(id)
}

actual fun createStepCounterPlugin(): StepCounterPlugin = IosStepCounterPlugin()
