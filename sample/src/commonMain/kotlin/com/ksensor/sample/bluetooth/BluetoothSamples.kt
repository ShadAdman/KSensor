package com.ksensor.sample.bluetooth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.states.bluetooth.BluetoothPlugin
import com.ksensor.plugins.states.bluetooth.createBluetoothPlugin

@Composable
fun BluetoothSample() {
    val bluetoothPlugin = remember {
        KSensor.get<BluetoothPlugin>(PluginId.BLUETOOTH)
            ?: createBluetoothPlugin().also { KSensor.register(it) }
    }

    val connectionsResponse by (bluetoothPlugin.connections().observe()).collectAsState(null)
    val discoveriesResponse by (bluetoothPlugin.discoveries().observe()).collectAsState(null)

    val connections = connectionsResponse?.data
    val discoveries = discoveriesResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bluetooth Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Connected Devices:", style = MaterialTheme.typography.h6)
        LazyColumn(modifier = Modifier.height(120.dp)) {
            items(connections?.connectedDevices ?: emptyList()) { device ->
                Text("• ${device.name} (${device.id})")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Discovered Devices:", style = MaterialTheme.typography.h6)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(discoveries?.discoveredDevices ?: emptyList()) { device ->
                Text("• ${device.name} (${device.id})")
            }
        }
    }
}

@Composable
fun AudioDevicesSample() {
    val bluetoothPlugin = remember {
        KSensor.get<BluetoothPlugin>(PluginId.BLUETOOTH)
            ?: createBluetoothPlugin().also { KSensor.register(it) }
    }

    val audioResponse by (bluetoothPlugin.audioDevices().observe()).collectAsState(null)
    val audio = audioResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bluetooth Audio Devices Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Connected Audio Devices:", style = MaterialTheme.typography.h6)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(audio?.connectedDevices ?: emptyList()) { device ->
                Text("• ${device.name} (${device.id})")
            }
        }
    }
}
