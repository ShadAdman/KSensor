package com.ksensor.core.context

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object KSensorContext {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun get(): Context {
        return context ?: throw IllegalStateException("KSensorContext not initialized. Ensure androidx.startup is working or call init() manually.")
    }
}
