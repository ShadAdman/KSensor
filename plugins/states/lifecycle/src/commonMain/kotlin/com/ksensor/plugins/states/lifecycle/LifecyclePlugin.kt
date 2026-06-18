package com.ksensor.plugins.states.lifecycle

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData

interface LifecyclePlugin : KSensorPlugin {
    fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus>
}

expect fun createLifecyclePlugin(): LifecyclePlugin
