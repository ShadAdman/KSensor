package com.ksensor.plugins.sensors.motion.gyroscope

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

class IosGyroscopePlugin : GyroscopePlugin {
    private val motionManager = CMMotionManager()

    @OptIn(ExperimentalForeignApi::class)
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Gyroscope>> = callbackFlow {
        if (!motionManager.gyroAvailable) {
            close()
            return@callbackFlow
        }

        motionManager.gyroUpdateInterval = config.intervalMs / 1000.0
        motionManager.startGyroUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.rotationRate?.useContents {
                val sensorData = SensorData.Gyroscope(Vector3(x.toFloat(), y.toFloat(), z.toFloat()))
                trySend(KSensorResponse(sensorData))
            }
        }

        awaitClose { motionManager.stopGyroUpdates() }
    }
}

actual fun createGyroscopePlugin(): GyroscopePlugin = IosGyroscopePlugin()
