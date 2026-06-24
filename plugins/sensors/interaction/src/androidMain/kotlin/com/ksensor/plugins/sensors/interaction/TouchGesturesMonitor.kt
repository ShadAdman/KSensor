package com.ksensor.plugins.sensors.interaction

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import android.view.MotionEvent
import android.view.Window
import androidx.annotation.RequiresApi
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.TouchGestureType

internal object TouchGesturesMonitor {
    private val context: Context by lazy { KSensorContext.get() }
    private val app = context.applicationContext as Application

    @Volatile
    private var observer: ((SensorData.TouchGestures) -> Unit)? = null

    fun registerObserver(onData: (SensorData.TouchGestures) -> Unit) {
        observer = onData
        app.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks)
    }

    fun removeObserver() {
        observer = null
        app.unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks)
    }

    private fun hookWindowCallback(activity: Activity) {
        val window = activity.window
        if (window.callback is TouchInterceptingCallback) return

        val originalCallback = window.callback
        window.callback = TouchInterceptingCallback(originalCallback) {
            observer?.invoke(it)
        }
    }

    private object ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            hookWindowCallback(activity)
        }
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
    }
}

internal class TouchInterceptingCallback(
    private val delegate: Window.Callback,
    private val onData: (SensorData.TouchGestures) -> Unit
) : Window.Callback by delegate {

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val type = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> TouchGestureType.ACTION_DOWN
            MotionEvent.ACTION_MOVE -> TouchGestureType.ACTION_MOVE
            MotionEvent.ACTION_UP -> TouchGestureType.ACTION_UP
            else -> null
        }

        type?.let {
            onData(SensorData.TouchGestures(event.rawX, event.rawY, it))
        }

        return delegate.dispatchTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        delegate.onPointerCaptureChanged(hasCapture)
    }

    override fun onProvideKeyboardShortcuts(
        data: List<KeyboardShortcutGroup?>?,
        menu: Menu?,
        deviceId: Int
    ) {
        delegate.onProvideKeyboardShortcuts(data, menu, deviceId)
    }
}
