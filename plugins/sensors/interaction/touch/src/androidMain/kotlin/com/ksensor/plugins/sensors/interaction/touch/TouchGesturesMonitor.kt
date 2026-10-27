package com.ksensor.plugins.sensors.interaction.touch

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.TouchGestureType

@SuppressLint("StaticFieldLeak")
internal object TouchGesturesMonitor : Application.ActivityLifecycleCallbacks {

    private var observer: ((SensorData.TouchGestures) -> Unit)? = null

    private var currentActivity: Activity? = null
    private var isRegistered = false

    fun registerObserver(obs: (SensorData.TouchGestures) -> Unit) {
        this.observer = obs
        if (!isRegistered) {
            val app = KSensorContext.get().applicationContext as Application
            app.registerActivityLifecycleCallbacks(this)
            isRegistered = true
        }
    }

    fun removeObserver() {
        this.observer = null
    }

    private fun wrapWindowCallback(activity: Activity) {
        val window = activity.window
        val originalCallback = window.callback ?: return

        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                observer?.invoke(SensorData.TouchGestures(e.x, e.y, TouchGestureType.TAP))
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                observer?.invoke(SensorData.TouchGestures(e.x, e.y, TouchGestureType.DOUBLE_TAP))
                return super.onDoubleTap(e)
            }

            override fun onLongPress(e: MotionEvent) {
                observer?.invoke(SensorData.TouchGestures(e.x, e.y, TouchGestureType.LONG_PRESS))
                super.onLongPress(e)
            }
        })

        val wrappedCallback = object : Window.Callback by originalCallback {
            override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
                if (event != null) {
                    gestureDetector.onTouchEvent(event)
                }
                return originalCallback.dispatchTouchEvent(event)
            }
        }
        window.callback = wrappedCallback
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        wrapWindowCallback(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
