package com.ksensor.plugins.sensors.positioning.magnetometer

import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

class IosMagnetometerPlugin : MagnetometerPlugin {
    private val motionManager = CMMotionManager()

    @OptIn(ExperimentalForeignApi::class)
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Magnetometer>> = callbackFlow {
        if (!motionManager.magnetometerAvailable) {
            KSensor.reportStatus(
                pluginId = id,
                status = PluginStatus.UNSUPPORTED,
                unsupportedReason = "Magnetometer hardware is not available on this iOS device"
            )
            close()
            return@callbackFlow
        }
        motionManager.magnetometerUpdateInterval = config.intervalMs / 1000.0
        motionManager.startMagnetometerUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.magneticField?.useContents {
                val sensorData = SensorData.Magnetometer(Vector3(x.toFloat(), y.toFloat(), z.toFloat()))
                trySend(KSensorResponse(sensorData))
            }
        }
        awaitClose { motionManager.stopMagnetometerUpdates() }
    }.trackPluginDiagnostics(id)
}

actual fun createMagnetometerPlugin(): MagnetometerPlugin = IosMagnetometerPlugin()
