package com.ksensor.sample.positioning

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.positioning.PositioningPlugin
import com.ksensor.plugins.sensors.positioning.createPositioningPlugin
import com.ksensor.sample.MetricCard
import com.ksensor.sample.StatusBadge
import com.ksensor.sample.toTwoDecimalString

@Composable
fun LocationSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val location by plugin.location().collectAsState(null)

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Location", style = MaterialTheme.typography.h5)

        MetricCard(title = "Latitude", value = location?.data?.latitude?.toString() ?: "N/A")
        MetricCard(title = "Longitude", value = location?.data?.longitude?.toString() ?: "N/A")
        MetricCard(title = "Altitude", value = location?.data?.altitude?.toString() ?: "N/A")
    }
}

@Composable
fun HeadingSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val headingResponse by plugin.heading().collectAsState(null)
    val heading = headingResponse?.data

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Heading", style = MaterialTheme.typography.h5)

        if (heading != null) {
            MetricCard(title = "Magnetic Heading", value = "${heading.magneticHeading.toTwoDecimalString()}°")
            MetricCard(title = "True Heading", value = "${heading.trueHeading.toTwoDecimalString()}°")
            MetricCard(title = "Device Heading", value = "${heading.deviceHeading.toTwoDecimalString()}°")
            MetricCard(title = "Course Over Ground", value = "${heading.courseOverGround.toTwoDecimalString()}°")
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for heading data...")
                }
            }
        }
    }
}

@Composable
fun OrientationSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val orientation by plugin.orientation().collectAsState(null)

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Orientation", style = MaterialTheme.typography.h5)

        if (orientation != null) {
            MetricCard(title = "Device Orientation", value = orientation?.data?.orientation?.name ?: "N/A")
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for orientation data...")
                }
            }
        }
    }
}

@Composable
fun MagnetometerSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val magnetometer by plugin.magnetometer().collectAsState(null)

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Magnetometer", style = MaterialTheme.typography.h5)

        if (magnetometer != null) {
            val data = magnetometer?.data
            MetricCard(title = "X Axis", value = "${data?.values?.x?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
            MetricCard(title = "Y Axis", value = "${data?.values?.y?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
            MetricCard(title = "Z Axis", value = "${data?.values?.z?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Waiting for magnetometer data...")
                }
            }
        }
    }
}

@Composable
fun LocationStatusSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val statusResponse by plugin.locationStatus().observe().collectAsState(null)
    val status = statusResponse?.data

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Location Status", style = MaterialTheme.typography.h5)

        if (status != null) {
            StatusBadge(
                text = if (status.isLocationOn) "GPS Enabled" else "GPS Disabled",
                isActive = status.isLocationOn
            )
        } else {
            Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Loading location status...")
                }
            }
        }
    }
}
