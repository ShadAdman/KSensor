package com.ksensor.core.context

import android.content.Context
import androidx.startup.Initializer

class KSensorInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        KSensorContext.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
