package com.ksensor.plugins.sensors.motion.motiondetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent) ?: return
            val mostProbableActivity = result.mostProbableActivity
            val type = when (mostProbableActivity.type) {
                DetectedActivity.IN_VEHICLE -> SensorData.MotionType.AUTOMOTIVE
                DetectedActivity.ON_BICYCLE -> SensorData.MotionType.CYCLING
                DetectedActivity.RUNNING -> SensorData.MotionType.RUNNING
                DetectedActivity.STILL -> SensorData.MotionType.STATIONARY
                DetectedActivity.WALKING -> SensorData.MotionType.WALKING
                else -> SensorData.MotionType.UNKNOWN
            }
            _motionEvents.tryEmit(type)
        }
    }

    companion object {
        private val _motionEvents = MutableSharedFlow<SensorData.MotionType>(extraBufferCapacity = 64)
        val motionEvents = _motionEvents.asSharedFlow()
    }
}
