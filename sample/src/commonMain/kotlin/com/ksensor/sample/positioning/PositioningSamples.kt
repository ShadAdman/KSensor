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
import com.ksensor.sample.toTwoDecimalString

@Composable
fun LocationSample() {
    val plugin = remember {
        KSensor.get<PositioningPlugin>(PluginId.POSITIONING)
            ?: createPositioningPlugin().also { KSensor.register(it) }
    }

    val location by plugin.location().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Location Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Location:", style = MaterialTheme.typography.h6)
        Text("Latitude: ${location?.data?.latitude ?: "N/A"}")
        Text("Longitude: ${location?.data?.longitude ?: "N/A"}")
        Text("Altitude: ${location?.data?.altitude ?: "N/A"}")
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Heading Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (heading != null) {
            Text("Magnetic Heading: ${heading.magneticHeading.toTwoDecimalString()}°")
            Text("True Heading: ${heading.trueHeading.toTwoDecimalString()}°")
            Text("Device Heading: ${heading.deviceHeading.toTwoDecimalString()}°")
            Text("Course Over Ground: ${heading.courseOverGround.toTwoDecimalString()}°")
        } else {
            Text("Waiting for heading data...")
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Orientation Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (orientation != null) {
            Text("Orientation Data: ${orientation?.data}")
        } else {
            Text("Waiting for orientation data...")
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Magnetometer Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (magnetometer != null) {
            val data = magnetometer?.data
            Text("X: ${data?.values?.x?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
            Text("Y: ${data?.values?.y?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
            Text("Z: ${data?.values?.z?.toDouble()?.toTwoDecimalString() ?: "N/A"} µT")
        } else {
            Text("Waiting for magnetometer data...")
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Location Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (status != null) {
            Text("Is Location On: ${status.isLocationOn}")
        } else {
            Text("Loading location status...")
        }
    }
}
