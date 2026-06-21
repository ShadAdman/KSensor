package com.ksensor.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.PluginId
import com.ksensor.plugins.states.bluetooth.BluetoothPlugin
import com.ksensor.plugins.states.bluetooth.createBluetoothPlugin
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BluetoothSample()
        }
    }
}

@Composable
fun BluetoothSample() {
    var isRegistered by remember { mutableStateOf(false) }
    val bluetoothPlugin = remember { 
        if (!isRegistered) {
            val plugin = createBluetoothPlugin()
            KSensor.register(plugin)
            isRegistered = true
            plugin
        } else {
            KSensor.get<BluetoothPlugin>(PluginId.BLUETOOTH)
        }
    }

    val connectionsResponse by (bluetoothPlugin?.connections()?.observe() ?: emptyFlow()).collectAsState(null)
    val discoveriesResponse by (bluetoothPlugin?.discoveries()?.observe() ?: emptyFlow()).collectAsState(null)

    val connections = connectionsResponse?.data
    val discoveries = discoveriesResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bluetooth Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Connected Devices:", style = MaterialTheme.typography.h6)
        LazyColumn(modifier = Modifier.height(100.dp)) {
            items(connections?.connectedDevices ?: emptyList()) { device ->
                Text("${device.name} (${device.id})")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Discovered Devices:", style = MaterialTheme.typography.h6)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(discoveries?.discoveredDevices ?: emptyList()) { device ->
                Text("${device.name} (${device.id})")
            }
        }
    }
}
