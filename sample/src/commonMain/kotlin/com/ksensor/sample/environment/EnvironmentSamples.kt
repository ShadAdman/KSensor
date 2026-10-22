package com.ksensor.sample.environment

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.environment.EnvironmentPlugin
import com.ksensor.plugins.sensors.environment.createEnvironmentPlugin
import com.ksensor.sample.toTwoDecimalString

@Composable
fun NoiseSample() {
    val plugin = remember {
        KSensor.get<EnvironmentPlugin>(PluginId.ENVIRONMENT)
            ?: createEnvironmentPlugin().also { KSensor.register(it) }
    }

    val noiseResponse by plugin.noise().collectAsState(null)
    val noise = noiseResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Noise Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (noise != null) {
            Text("Noise Level: ${noise.dB.toDouble().toTwoDecimalString()} dB")
            Text("Loud? ${noise.isLoud}")
        } else {
            Text("Loading noise data...")
        }
    }
}

@Composable
fun BarometerSample() {
    val plugin = remember {
        KSensor.get<EnvironmentPlugin>(PluginId.ENVIRONMENT)
            ?: createEnvironmentPlugin().also { KSensor.register(it) }
    }

    val barometer by plugin.barometer().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Barometer Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (barometer != null) {
            Text("Pressure: ${barometer?.data?.pressure?.toDouble()?.toTwoDecimalString() ?: "N/A"} hPa")
        } else {
            Text("Waiting for barometer data...")
        }
    }
}

@Composable
fun LightSample() {
    val plugin = remember {
        KSensor.get<EnvironmentPlugin>(PluginId.ENVIRONMENT)
            ?: createEnvironmentPlugin().also { KSensor.register(it) }
    }

    val light by plugin.light().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Light Sensor Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (light != null) {
            Text("Illuminance: ${light?.data?.illuminance?.toDouble()?.toTwoDecimalString() ?: "N/A"} lux")
        } else {
            Text("Waiting for light sensor data...")
        }
    }
}

@Composable
fun ProximitySample() {
    val plugin = remember {
        KSensor.get<EnvironmentPlugin>(PluginId.ENVIRONMENT)
            ?: createEnvironmentPlugin().also { KSensor.register(it) }
    }

    val proximity by plugin.proximity().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Proximity Sensor Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (proximity != null) {
            Text("Distance: ${proximity?.data?.distanceInCM?.toDouble()?.toTwoDecimalString() ?: "N/A"} cm")
            Text("Is Near: ${proximity?.data?.isNear == true}")
        } else {
            Text("Waiting for proximity data...")
        }
    }
}
