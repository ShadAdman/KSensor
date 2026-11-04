package com.ksensor.plugins.states.network

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import com.ksensor.plugins.states.network.activenetwork.ActiveNetworkStatusPlugin
import com.ksensor.plugins.states.network.activenetwork.createActiveNetworkStatusPlugin
import com.ksensor.plugins.states.network.callstate.CallStateStatusPlugin
import com.ksensor.plugins.states.network.callstate.createCallStateStatusPlugin
import com.ksensor.plugins.states.network.connectivity.ConnectivityStatusPlugin
import com.ksensor.plugins.states.network.connectivity.createConnectivityStatusPlugin

interface NetworkPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.NETWORK
    override val requiredPermissions: List<Permission> get() = listOf(Permission.READ_PHONE_STATE)

    fun connectivity(): StatePlugin<StateData.ConnectivityStatus>
    fun activeNetwork(): StatePlugin<StateData.CurrentActiveNetwork>
    fun callState(): StatePlugin<StateData.CallStateStatus>
}

class DefaultNetworkPlugin(
    private val connectivityPlugin: ConnectivityStatusPlugin = createConnectivityStatusPlugin(),
    private val activeNetworkPlugin: ActiveNetworkStatusPlugin = createActiveNetworkStatusPlugin(),
    private val callStatePlugin: CallStateStatusPlugin = createCallStateStatusPlugin()
) : NetworkPlugin {
    override fun connectivity(): StatePlugin<StateData.ConnectivityStatus> = connectivityPlugin
    override fun activeNetwork(): StatePlugin<StateData.CurrentActiveNetwork> = activeNetworkPlugin
    override fun callState(): StatePlugin<StateData.CallStateStatus> = callStatePlugin
}

fun createNetworkPlugin(): NetworkPlugin = DefaultNetworkPlugin()
