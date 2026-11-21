package com.ksensor.sample.fused

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.FusedConfidence
import com.ksensor.core.model.FusedSourceInfo
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.fused.FusedSensorPlugin
import com.ksensor.plugins.sensors.fused.createFusedSensorPlugin
import com.ksensor.sample.toTwoDecimalString

@Composable
fun FusedSensorShowcase() {
    val plugin = remember {
        KSensor.get<FusedSensorPlugin>(PluginId.FUSED_SENSOR)
            ?: createFusedSensorPlugin().also { KSensor.register(it) }
    }

    val activityState by plugin.activityClassification().collectAsState(null)
    val postureState by plugin.postureEstimation().collectAsState(null)
    val orientationState by plugin.fused3DOrientation().collectAsState(null)
    val surroundingState by plugin.fusedSurrounding().collectAsState(null)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Fused Sensors Showcase", style = MaterialTheme.typography.h4)
            Text(
                "High-level composite outputs combining raw streams with dynamic confidence scoring & source tracking.",
                style = MaterialTheme.typography.caption
            )
        }

        // 1. Motion Activity Classification
        item {
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Activity Classification", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (activityState != null) {
                        val data = activityState!!.data
                        Text("Classified Activity: ${data.result}", style = MaterialTheme.typography.subtitle1)
                        ConfidenceCard(data.confidence)
                        Spacer(modifier = Modifier.height(8.dp))
                        SourcesList(data.sources)
                    } else {
                        Text("Initializing fused activity classifier...")
                    }
                }
            }
        }

        // 2. Posture Estimation
        item {
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Posture Estimation", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (postureState != null) {
                        val data = postureState!!.data
                        Text("Estimated Posture: ${data.result}", style = MaterialTheme.typography.subtitle1)
                        ConfidenceCard(data.confidence)
                        Spacer(modifier = Modifier.height(8.dp))
                        SourcesList(data.sources)
                    } else {
                        Text("Initializing posture estimator...")
                    }
                }
            }
        }

        // 3. Fused Orientation Confidence
        item {
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fused Spatial Orientation", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (orientationState != null) {
                        val data = orientationState!!.data
                        Text("Orientation: ${data.orientation}")
                        Text("Pitch: ${data.pitchDegrees.toDouble().toTwoDecimalString()}° | Roll: ${data.rollDegrees.toDouble().toTwoDecimalString()}° | Yaw: ${data.yawDegrees.toDouble().toTwoDecimalString()}°")
                        Text("Spatial Stability: ${if (data.isStable) "Stable" else "In Motion"}")
                        ConfidenceCard(data.confidence)
                        Spacer(modifier = Modifier.height(8.dp))
                        SourcesList(data.sources)
                    } else {
                        Text("Initializing fused orientation...")
                    }
                }
            }
        }

        // 4. Device Surroundings Summary
        item {
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device Surroundings Summary", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (surroundingState != null) {
                        val data = surroundingState!!.data
                        Text("Environment: ${data.environmentState}")
                        Text("Motion State: ${data.motionState}")
                        Text("Indoor Estimate: ${if (data.isIndoorEstimate) "Yes" else "No"}")
                        Text("Noise Level: ${data.noiseLevelSummary}")
                        ConfidenceCard(data.confidence)
                        Spacer(modifier = Modifier.height(8.dp))
                        SourcesList(data.sources)
                    } else {
                        Text("Initializing surroundings summary...")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceCard(confidence: FusedConfidence) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Confidence: ${(confidence.score * 100).toInt()}% (${confidence.level})", style = MaterialTheme.typography.body2)
            Text("Quality: ${(confidence.quality * 100).toInt()}%", style = MaterialTheme.typography.body2)
        }
        LinearProgressIndicator(
            progress = confidence.score,
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(vertical = 2.dp)
        )
        confidence.explanation?.let { exp ->
            Text(exp, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun SourcesList(sources: List<FusedSourceInfo>) {
    Text("Active Sensor Sources:", style = MaterialTheme.typography.subtitle2)
    sources.forEach { source ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("- ${source.pluginId.name}", style = MaterialTheme.typography.caption)
            Text(
                if (source.isAvailable) "Active (weight ${(source.weight * 100).toInt()}%)" else "Unavailable",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
