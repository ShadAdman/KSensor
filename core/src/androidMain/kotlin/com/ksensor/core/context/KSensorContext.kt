package com.ksensor.core.context

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object KSensorContext {
    private var context: Context? = null
    private var currentActivity: WeakReference<Activity>? = null

    fun init(context: Context) {
        val appContext = context.applicationContext
        this.context = appContext
        
        if (appContext is Application) {
            appContext.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    currentActivity = WeakReference(activity)
                }
                override fun onActivityStarted(activity: Activity) {
                    currentActivity = WeakReference(activity)
                }
                override fun onActivityResumed(activity: Activity) {
                    currentActivity = WeakReference(activity)
                }
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity?.get() == activity) {
                        currentActivity = null
                    }
                }
            })
        }
    }

    fun get(): Context {
        return context ?: throw IllegalStateException("KSensorContext not initialized. Ensure androidx.startup is working or call init() manually.")
    }

    fun getActivity(): Activity? {
        return currentActivity?.get()
    }
}
