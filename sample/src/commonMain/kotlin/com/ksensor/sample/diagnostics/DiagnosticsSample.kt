package com.ksensor.sample.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ksensor.core.*
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.environment.createEnvironmentPlugin
import com.ksensor.plugins.sensors.health.createHealthPlugin
import com.ksensor.plugins.sensors.interaction.createInteractionPlugin
import com.ksensor.plugins.sensors.motion.createMotionPlugin
import com.ksensor.plugins.sensors.positioning.createPositioningPlugin
import com.ksensor.plugins.states.bluetooth.createBluetoothPlugin
import com.ksensor.plugins.states.lifecycle.createLifecyclePlugin
import com.ksensor.plugins.states.network.createNetworkPlugin
import com.ksensor.plugins.states.system.createSystemPlugin

@Composable
fun DiagnosticsSample() {
    val diagnosticsMap by KSensor.diagnosticsFlow.collectAsState()
    var selectedFilter by remember { mutableStateOf<PluginStatus?>(null) }
    var observerLogs by remember { mutableStateOf<List<String>>(emptyList()) }

    DisposableEffect(Unit) {
        val observer = PluginDiagnosticsObserver { diag ->
            val logMessage = "[${diag.pluginId.name}] Status: ${diag.status}" +
                    if (diag.lastErrorMessage != null) " Error: ${diag.lastErrorMessage}" else ""
            observerLogs = (listOf(logMessage) + observerLogs).take(20)
        }
        KSensor.addDiagnosticsObserver(observer)
        onDispose {
            KSensor.removeDiagnosticsObserver(observer)
        }
    }

    val allDiagnostics = diagnosticsMap.values.toList()
    val filteredDiagnostics = if (selectedFilter == null) {
        allDiagnostics
    } else {
        allDiagnostics.filter { it.status == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Plugin Lifecycle & Health Diagnostics",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inspect runtime health, active observations, permission blocks, errors, and platform availability.",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            registerAllPluginsForDemo()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Register All Plugins", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            KSensor.refreshDiagnostics()
                        }
                    ) {
                        Text("Refresh Diagnostics")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HealthSummaryRow(diagnostics = allDiagnostics)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        Text("Filter by Status:", style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    text = "ALL (${allDiagnostics.size})",
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            items(PluginStatus.values()) { status ->
                val count = allDiagnostics.count { it.status == status }
                FilterChip(
                    text = "${status.name} ($count)",
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredDiagnostics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No plugins matching selected filter.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredDiagnostics) { diag ->
                    PluginDiagnosticsCard(diag = diag)
                }

                if (observerLogs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFF212121),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Global Diagnostics Sink Log", color = Color.Green, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                observerLogs.forEach { log ->
                                    Text(log, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryRow(diagnostics: List<PluginDiagnostics>) {
    val active = diagnostics.count { it.status == PluginStatus.ACTIVE }
    val ready = diagnostics.count { it.status == PluginStatus.READY }
    val blocked = diagnostics.count { it.status == PluginStatus.BLOCKED_BY_PERMISSION }
    val unsupported = diagnostics.count { it.status == PluginStatus.UNSUPPORTED }
    val error = diagnostics.count { it.status == PluginStatus.ERROR }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatBadge(label = "Active", count = active, color = Color(0xFF2E7D32))
        StatBadge(label = "Ready", count = ready, color = Color(0xFF1976D2))
        StatBadge(label = "Blocked", count = blocked, color = Color(0xFFED6C02))
        StatBadge(label = "Unsupported", count = unsupported, color = Color(0xFF757575))
        StatBadge(label = "Error", count = error, color = Color(0xFFD32F2F))
    }
}

@Composable
private fun StatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", style = MaterialTheme.typography.h6, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.caption, color = Color.Gray)
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colors.primary else Color(0xFFE0E0E0),
        modifier = Modifier.height(32.dp)
    ) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
            Text(
                text = text,
                color = if (selected) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PluginDiagnosticsCard(diag: PluginDiagnostics) {
    val statusColor = when (diag.status) {
        PluginStatus.ACTIVE -> Color(0xFF2E7D32)
        PluginStatus.READY -> Color(0xFF1976D2)
        PluginStatus.IDLE -> Color(0xFF0288D1)
        PluginStatus.BLOCKED_BY_PERMISSION -> Color(0xFFED6C02)
        PluginStatus.UNSUPPORTED -> Color(0xFF757575)
        PluginStatus.ERROR -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = diag.pluginId.name,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = diag.status.name,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Permission: ${diag.permissionState.name}",
                    style = MaterialTheme.typography.caption,
                    color = if (diag.permissionState == PermissionStatus.GRANTED) Color(0xFF2E7D32) else Color(0xFFED6C02)
                )

                Text(
                    text = if (diag.isPlatformAvailable) "Platform: Available" else "Platform: Unsupported",
                    style = MaterialTheme.typography.caption,
                    color = if (diag.isPlatformAvailable) Color.DarkGray else Color.Red
                )
            }

            if (diag.unsupportedReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Reason: ${diag.unsupportedReason}",
                        style = MaterialTheme.typography.caption,
                        color = Color.DarkGray
                    )
                }
            }

            if (diag.lastErrorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Last Error: ${diag.lastErrorMessage}",
                        style = MaterialTheme.typography.caption,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun registerAllPluginsForDemo() {
    if (!KSensor.hasPlugin(PluginId.POSITIONING)) KSensor.register(createPositioningPlugin())
    if (!KSensor.hasPlugin(PluginId.MOTION)) KSensor.register(createMotionPlugin())
    if (!KSensor.hasPlugin(PluginId.HEALTH)) KSensor.register(createHealthPlugin())
    if (!KSensor.hasPlugin(PluginId.ENVIRONMENT)) KSensor.register(createEnvironmentPlugin())
    if (!KSensor.hasPlugin(PluginId.BLUETOOTH)) KSensor.register(createBluetoothPlugin())
    if (!KSensor.hasPlugin(PluginId.SYSTEM)) KSensor.register(createSystemPlugin())
    if (!KSensor.hasPlugin(PluginId.NETWORK)) KSensor.register(createNetworkPlugin())
    if (!KSensor.hasPlugin(PluginId.INTERACTION)) KSensor.register(createInteractionPlugin())
    if (!KSensor.hasPlugin(PluginId.LIFECYCLE)) KSensor.register(createLifecyclePlugin())
}
