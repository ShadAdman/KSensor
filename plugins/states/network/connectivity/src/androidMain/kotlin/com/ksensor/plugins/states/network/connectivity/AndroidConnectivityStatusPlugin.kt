package com.ksensor.plugins.states.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

class AndroidConnectivityStatusPlugin : ConnectivityStatusPlugin {
    private val connectivityManager by lazy {
        KSensorContext.get().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.ConnectivityStatus>
        get() = KSensorResponse(StateData.ConnectivityStatus(isConnected()))

    private val connectivityFlow by lazy {
        callbackFlow {
            val monitor = ConnectivityMonitor(
                onStatusChanged = { trySend(KSensorResponse(StateData.ConnectivityStatus(it))) },
                onActiveNetworkChanged = {}
            )
            connectivityManager.registerDefaultNetworkCallback(monitor)
            awaitClose { connectivityManager.unregisterNetworkCallback(monitor) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.ConnectivityStatus>> = connectivityFlow

    private fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

actual fun createConnectivityStatusPlugin(): ConnectivityStatusPlugin = AndroidConnectivityStatusPlugin()
