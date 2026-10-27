package com.ksensor.plugins.states.network.activenetwork

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_get_main_queue

class IosActiveNetworkStatusPlugin : ActiveNetworkStatusPlugin {
    override val currentState: KSensorResponse<StateData.CurrentActiveNetwork>
        get() = KSensorResponse(StateData.CurrentActiveNetwork(StateData.CurrentActiveNetwork.ActiveNetwork.NONE))

    override fun observe(): Flow<KSensorResponse<StateData.CurrentActiveNetwork>> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val activeNetwork = when {
                nw_path_uses_interface_type(path, nw_interface_type_wifi) -> StateData.CurrentActiveNetwork.ActiveNetwork.WIFI
                nw_path_uses_interface_type(path, nw_interface_type_cellular) -> StateData.CurrentActiveNetwork.ActiveNetwork.CELLULAR
                else -> StateData.CurrentActiveNetwork.ActiveNetwork.NONE
            }
            trySend(KSensorResponse(StateData.CurrentActiveNetwork(activeNetwork)))
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}

actual fun createActiveNetworkStatusPlugin(): ActiveNetworkStatusPlugin = IosActiveNetworkStatusPlugin()
