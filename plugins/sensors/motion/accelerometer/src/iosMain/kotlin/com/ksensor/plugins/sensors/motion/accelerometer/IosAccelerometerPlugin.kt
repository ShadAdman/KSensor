package com.ksensor.plugins.sensors.motion.accelerometer

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

class IosAccelerometerPlugin : AccelerometerPlugin {
    private val motionManager = CMMotionManager()

    @OptIn(ExperimentalForeignApi::class)
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Accelerometer>> = callbackFlow {
        if (!motionManager.accelerometerAvailable) {
            close()
            return@callbackFlow
        }

        motionManager.accelerometerUpdateInterval = config.intervalMs / 1000.0
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.acceleration?.useContents {
                val sensorData = SensorData.Accelerometer(Vector3(x.toFloat(), y.toFloat(), z.toFloat()))
                trySend(KSensorResponse(sensorData))
            }
        }

        awaitClose { motionManager.stopAccelerometerUpdates() }
    }
}

actual fun createAccelerometerPlugin(): AccelerometerPlugin = IosAccelerometerPlugin()
