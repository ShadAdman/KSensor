package com.ksensor.plugins.states.network

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData

interface NetworkPlugin : KSensorPlugin {
    fun connectivity(): StatePlugin<StateData.ConnectivityStatus>
    fun activeNetwork(): StatePlugin<StateData.CurrentActiveNetwork>
}

expect fun createNetworkPlugin(): NetworkPlugin
