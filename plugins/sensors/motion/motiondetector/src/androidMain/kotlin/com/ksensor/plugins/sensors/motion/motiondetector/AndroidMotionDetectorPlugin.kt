package com.ksensor.plugins.sensors.motion.motiondetector

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.ActivityRecognition
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidMotionDetectorPlugin : MotionDetectorPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val activityRecognitionClient by lazy {
        ActivityRecognition.getClient(context)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.MotionDetector>>>()

    @SuppressLint("MissingPermission")
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.MotionDetector>> =
        flows.getOrPut(config) {
            callbackFlow {
                val intent = Intent(context, ActivityRecognitionReceiver::class.java)
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    flags
                )

                activityRecognitionClient.requestActivityUpdates(
                    config.intervalMs,
                    pendingIntent
                )

                val job = ActivityRecognitionReceiver.motionEvents
                    .onEach { type ->
                        trySend(KSensorResponse(SensorData.MotionDetector(type)))
                    }
                    .launchIn(scope)

                awaitClose {
                    job.cancel()
                    activityRecognitionClient.removeActivityUpdates(pendingIntent)
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createMotionDetectorPlugin(): MotionDetectorPlugin = AndroidMotionDetectorPlugin()
