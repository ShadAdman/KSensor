package com.ksensor.sample.health

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.SensorData
import com.ksensor.plugins.sensors.health.HealthPlugin
import com.ksensor.plugins.sensors.health.createHealthPlugin

@Composable
fun HealthSample() {
    val plugin = remember {
        KSensor.get<HealthPlugin>(PluginId.HEALTH)
            ?: createHealthPlugin().also { KSensor.register(it) }
    }

    val heartRateResponse by plugin.heartRate().collectAsState(null)

    var lastValidHeartRate by remember { mutableStateOf<SensorData.HeartRate?>(null) }

    LaunchedEffect(heartRateResponse) {
        val data = heartRateResponse?.data
        if (data != null && (data.heartRate > 0 || lastValidHeartRate == null)) {
            lastValidHeartRate = data
        }
    }

    val heartRate = lastValidHeartRate

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Health Plugin Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (heartRate != null) {
            if (heartRate.source == SensorData.HeartRateSource.CAMERA_PPG) {
                if (heartRate.heartRate == 0f) {
                    val status = "Place your finger over the camera lens and flash"
                    Text("Camera PPG Active", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                    Text(status, style = MaterialTheme.typography.body1)
                } else {
                    val color = if (heartRate.confidence > 0.6f) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
                    Text("Heart Rate: ${heartRate.heartRate.toInt()} BPM", style = MaterialTheme.typography.h4)
                    Text("Source: Camera PPG (Confidence: ${(heartRate.confidence * 100).toInt()}%)",
                        style = MaterialTheme.typography.caption,
                        color = color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Keep your finger on the camera.", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.secondary)
                }
            } else {
                Text("Heart Rate: ${heartRate.heartRate.toInt()} BPM", style = MaterialTheme.typography.h4)
                Text("Source: Hardware Sensor", style = MaterialTheme.typography.caption)
            }
        } else {
            Text("Waiting for heart rate data...")
            Text("(Make sure you have a heart rate sensor and permission is granted)")
        }
    }
}
