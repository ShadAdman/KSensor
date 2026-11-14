package com.ksensor.plugins.sensors.motion.motiondetector

import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSOperationQueue

class IosMotionDetectorPlugin : MotionDetectorPlugin {
    override val isSupported: Boolean get() = CMMotionActivityManager.isActivityAvailable()
    override val unsupportedReason: String? get() = if (!isSupported) "Motion activity detection is not supported on this iOS device" else null

    private val activityManager = CMMotionActivityManager()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.MotionDetector>> = callbackFlow {
        if (!CMMotionActivityManager.isActivityAvailable()) {
            KSensor.reportStatus(
                pluginId = id,
                status = PluginStatus.UNSUPPORTED,
                unsupportedReason = unsupportedReason
            )
            close()
            return@callbackFlow
        }

        activityManager.startActivityUpdatesToQueue(NSOperationQueue.mainQueue()) { activity ->
            activity?.let {
                val type = when {
                    it.running -> SensorData.MotionType.RUNNING
                    it.cycling -> SensorData.MotionType.CYCLING
                    it.walking -> SensorData.MotionType.WALKING
                    it.automotive -> SensorData.MotionType.AUTOMOTIVE
                    it.stationary -> SensorData.MotionType.STATIONARY
                    else -> SensorData.MotionType.UNKNOWN
                }
                trySend(KSensorResponse(SensorData.MotionDetector(type)))
            }
        }

        awaitClose { activityManager.stopActivityUpdates() }
    }.trackPluginDiagnostics(id)
}

actual fun createMotionDetectorPlugin(): MotionDetectorPlugin = IosMotionDetectorPlugin()
