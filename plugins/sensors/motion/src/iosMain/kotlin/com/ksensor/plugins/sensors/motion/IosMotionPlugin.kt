package com.ksensor.plugins.sensors.motion

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.Vector3
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMMotionManager
import platform.CoreMotion.CMPedometer
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970

class IosMotionPlugin : MotionPlugin {
    override val id: String = "ksensor.sensors.motion"
    override val requiredPermissions: List<Permission> = emptyList()

    private val motionManager = CMMotionManager()
    private val pedometer = if (CMPedometer.isStepCountingAvailable()) CMPedometer() else null

    @OptIn(ExperimentalForeignApi::class)
    override fun accelerometer(config: SensorConfig): Flow<SensorData.Accelerometer> = callbackFlow {
        if (!motionManager.accelerometerAvailable) {
            close()
            return@callbackFlow
        }

        motionManager.accelerometerUpdateInterval = config.samplingIntervalMs / 1000.0
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.acceleration?.useContents {
                trySend(SensorData.Accelerometer(Vector3(x.toFloat(), y.toFloat(), z.toFloat())))
            }
        }

        awaitClose { motionManager.stopAccelerometerUpdates() }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun gyroscope(config: SensorConfig): Flow<SensorData.Gyroscope> = callbackFlow {
        if (!motionManager.gyroAvailable) {
            close()
            return@callbackFlow
        }

        motionManager.gyroUpdateInterval = config.samplingIntervalMs / 1000.0
        motionManager.startGyroUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.rotationRate?.useContents {
                trySend(SensorData.Gyroscope(Vector3(x.toFloat(), y.toFloat(), z.toFloat())))
            }
        }

        awaitClose { motionManager.stopGyroUpdates() }
    }

    override fun stepCounter(config: SensorConfig): Flow<SensorData.StepCounter> = callbackFlow {
        if (pedometer == null) {
            close()
            return@callbackFlow
        }

        pedometer.startPedometerUpdatesFromDate(NSDate()) { data, _ ->
            data?.let {
                trySend(SensorData.StepCounter(it.numberOfSteps.intValue))
            }
        }

        awaitClose { pedometer.stopPedometerUpdates() }
    }

    override fun stepDetector(config: SensorConfig): Flow<SensorData.StepDetector> = callbackFlow {
        if (pedometer == null) {
            close()
            return@callbackFlow
        }

        pedometer.startPedometerUpdatesFromDate(NSDate()) { data, _ ->
            if (data != null) {
                trySend(SensorData.StepDetector)
            }
        }

        awaitClose { pedometer.stopPedometerUpdates() }
    }
}

actual fun createMotionPlugin(): MotionPlugin = IosMotionPlugin()
