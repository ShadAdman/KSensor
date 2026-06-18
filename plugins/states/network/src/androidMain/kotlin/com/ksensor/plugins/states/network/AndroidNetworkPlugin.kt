package com.ksensor.plugins.states.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidNetworkPlugin : NetworkPlugin {
    override val id: String = "ksensor.states.network"
    override val requiredPermissions: List<Permission> = emptyList()

    private val connectivityManager by lazy {
        KSensorContext.get().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun connectivity(): StatePlugin<StateData.ConnectivityStatus> {
        return object : StatePlugin<StateData.ConnectivityStatus> {
            override val id: String = "${this@AndroidNetworkPlugin.id}.connectivity"
            override val requiredPermissions: List<Permission> = emptyList()
            override val currentState: StateData.ConnectivityStatus
                get() = StateData.ConnectivityStatus(isConnected())

            override fun observe(): Flow<StateData.ConnectivityStatus> = callbackFlow {
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(StateData.ConnectivityStatus(true))
                    }
                    override fun onLost(network: Network) {
                        trySend(StateData.ConnectivityStatus(false))
                    }
                }
                connectivityManager.registerDefaultNetworkCallback(callback)
                awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
            }
        }
    }

    override fun activeNetwork(): StatePlugin<StateData.CurrentActiveNetwork> {
        return object : StatePlugin<StateData.CurrentActiveNetwork> {
            override val id: String = "${this@AndroidNetworkPlugin.id}.active"
            override val requiredPermissions: List<Permission> = emptyList()
            override val currentState: StateData.CurrentActiveNetwork
                get() = StateData.CurrentActiveNetwork(getActiveNetworkType())

            override fun observe(): Flow<StateData.CurrentActiveNetwork> = callbackFlow {
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                        val type = when {
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> StateData.CurrentActiveNetwork.ActiveNetwork.WIFI
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> StateData.CurrentActiveNetwork.ActiveNetwork.CELLULAR
                            else -> StateData.CurrentActiveNetwork.ActiveNetwork.NONE
                        }
                        trySend(StateData.CurrentActiveNetwork(type))
                    }
                    override fun onLost(network: Network) {
                        trySend(StateData.CurrentActiveNetwork(StateData.CurrentActiveNetwork.ActiveNetwork.NONE))
                    }
                }
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(request, callback)
                awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
            }
        }
    }

    private fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getActiveNetworkType(): StateData.CurrentActiveNetwork.ActiveNetwork {
        val network = connectivityManager.activeNetwork ?: return StateData.CurrentActiveNetwork.ActiveNetwork.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return StateData.CurrentActiveNetwork.ActiveNetwork.NONE
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> StateData.CurrentActiveNetwork.ActiveNetwork.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> StateData.CurrentActiveNetwork.ActiveNetwork.CELLULAR
            else -> StateData.CurrentActiveNetwork.ActiveNetwork.NONE
        }
    }
}

actual fun createNetworkPlugin(): NetworkPlugin = AndroidNetworkPlugin()
