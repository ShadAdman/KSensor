package com.ksensor.plugins.states.network.activenetwork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class AndroidActiveNetworkStatusPlugin : ActiveNetworkStatusPlugin {
    private val connectivityManager by lazy {
        KSensorContext.get().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.CurrentActiveNetwork>
        get() = KSensorResponse(StateData.CurrentActiveNetwork(getActiveNetworkType()))

    private val activeNetworkFlow by lazy {
        callbackFlow {
            val monitor = ConnectivityMonitor(
                onStatusChanged = {},
                onActiveNetworkChanged = { trySend(KSensorResponse(StateData.CurrentActiveNetwork(it))) }
            )
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, monitor)
            awaitClose { connectivityManager.unregisterNetworkCallback(monitor) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.CurrentActiveNetwork>> = activeNetworkFlow

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

actual fun createActiveNetworkStatusPlugin(): ActiveNetworkStatusPlugin = AndroidActiveNetworkStatusPlugin()
