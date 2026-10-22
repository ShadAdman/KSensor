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
import com.ksensor.sample.MetricCard
import com.ksensor.sample.SparklineChart
import com.ksensor.sample.StatusBadge
import com.ksensor.sample.toTwoDecimalString

@Composable
fun NoiseSample() {
    val plugin = remember {
        KSensor.get<EnvironmentPlugin>(PluginId.ENVIRONMENT)
            ?: createEnvironmentPlugin().also { KSensor.register(it) }
    }

    val noiseResponse by plugin.noise().collectAsState(null)
    val noise = noiseResponse?.data

    var history by remember { mutableStateOf(listOf<Float>()) }
    LaunchedEffect(noise?.dB) {
        if (noise != null) {
            history = history + noise.dB
        }
    }

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Noise Meter", style = MaterialTheme.typography.h5)

        if (noise != null) {
            MetricCard(
                title = "Ambient Sound Level",
                value = "${noise.dB.toDouble().toTwoDecimalString()} dB",
                subtitle = if (noise.isLoud == true) "High noise detected" else "Normal sound level"
            )

            StatusBadge(
                text = if (noise.isLoud == true) "Loud Environment" else "Quiet Environment",
                isActive = noise.isLoud != true
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Noise History Chart", style = MaterialTheme.typography.subtitle2)
            SparklineChart(values = history)
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Listening to ambient environment...")
                }
            }
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

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Barometer", style = MaterialTheme.typography.h5)

        if (barometer != null) {
            MetricCard(
                title = "Atmospheric Pressure",
                value = "${barometer?.data?.pressure?.toDouble()?.toTwoDecimalString() ?: "N/A"} hPa"
            )
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for barometer data...")
                }
            }
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

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Light Sensor", style = MaterialTheme.typography.h5)

        if (light != null) {
            MetricCard(
                title = "Illuminance",
                value = "${light?.data?.illuminance?.toDouble()?.toTwoDecimalString() ?: "N/A"} lux"
            )
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for light sensor data...")
                }
            }
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

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Proximity Sensor", style = MaterialTheme.typography.h5)

        if (proximity != null) {
            val data = proximity?.data
            MetricCard(
                title = "Distance",
                value = "${data?.distanceInCM?.toDouble()?.toTwoDecimalString() ?: "N/A"} cm"
            )
            StatusBadge(
                text = if (data?.isNear == true) "Object Near" else "Object Far",
                isActive = data?.isNear == true
            )
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for proximity data...")
                }
            }
        }
    }
}
