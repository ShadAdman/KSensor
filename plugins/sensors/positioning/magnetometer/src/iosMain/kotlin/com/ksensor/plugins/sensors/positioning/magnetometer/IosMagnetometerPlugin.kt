package com.ksensor.plugins.sensors.positioning.magnetometer

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
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
    }
}

actual fun createMagnetometerPlugin(): MagnetometerPlugin = IosMagnetometerPlugin()
