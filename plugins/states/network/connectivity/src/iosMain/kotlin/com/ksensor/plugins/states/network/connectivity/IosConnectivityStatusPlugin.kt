package com.ksensor.plugins.states.network.connectivity

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

class IosConnectivityStatusPlugin : ConnectivityStatusPlugin {
    override val currentState: KSensorResponse<StateData.ConnectivityStatus>
        get() = KSensorResponse(StateData.ConnectivityStatus(false))

    override fun observe(): Flow<KSensorResponse<StateData.ConnectivityStatus>> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            val sensorData = StateData.ConnectivityStatus(status == nw_path_status_satisfied)
            trySend(KSensorResponse(sensorData))
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}

actual fun createConnectivityStatusPlugin(): ConnectivityStatusPlugin = IosConnectivityStatusPlugin()
