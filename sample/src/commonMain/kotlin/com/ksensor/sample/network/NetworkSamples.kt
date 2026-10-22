package com.ksensor.sample.network

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.states.network.NetworkPlugin
import com.ksensor.plugins.states.network.createNetworkPlugin

@Composable
fun CallStateSample() {
    val plugin = remember {
        KSensor.get<NetworkPlugin>(PluginId.NETWORK)
            ?: createNetworkPlugin().also { KSensor.register(it) }
    }

    val callStateResponse by plugin.callState().observe().collectAsState(null)
    val callStateStatus = callStateResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Call State Sample (via NetworkPlugin)", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (callStateStatus != null) {
            Text("State: ${callStateStatus.state}", style = MaterialTheme.typography.h6)
            Text("Is In Call: ${callStateStatus.isInCall}")
        } else {
            Text("Loading call data...")
        }
    }
}

@Composable
fun ConnectivitySample() {
    val plugin = remember {
        KSensor.get<NetworkPlugin>(PluginId.NETWORK)
            ?: createNetworkPlugin().also { KSensor.register(it) }
    }

    val connectivity by plugin.connectivity().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Connectivity Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (connectivity != null) {
            val data = connectivity?.data
            Text("Is Connected: ${data?.isConnected == true}")
        } else {
            Text("Loading connectivity data...")
        }
    }
}

@Composable
fun ActiveNetworkSample() {
    val plugin = remember {
        KSensor.get<NetworkPlugin>(PluginId.NETWORK)
            ?: createNetworkPlugin().also { KSensor.register(it) }
    }

    val activeNetwork by plugin.activeNetwork().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Active Network Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (activeNetwork != null) {
            val data = activeNetwork?.data
            Text("Active Network Type: ${data?.activeNetwork}")
        } else {
            Text("Loading active network data...")
        }
    }
}
