package com.ksensor.plugins.states.network.activenetwork

import android.net.ConnectivityManager
import android.net.Network
import com.ksensor.core.model.StateData

internal class ConnectivityMonitor(
    private val onStatusChanged: (Boolean) -> Unit,
    private val onActiveNetworkChanged: (StateData.CurrentActiveNetwork.ActiveNetwork) -> Unit
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        onStatusChanged(true)
    }

    override fun onLost(network: Network) {
        onStatusChanged(false)
        onActiveNetworkChanged(StateData.CurrentActiveNetwork.ActiveNetwork.NONE)
    }
}
