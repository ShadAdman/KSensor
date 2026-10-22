package com.ksensor.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.KSensorPlugin
import com.ksensor.core.PermissionStatus
import com.ksensor.core.model.PluginId

import com.ksensor.sample.positioning.*
import com.ksensor.sample.motion.*
import com.ksensor.sample.health.*
import com.ksensor.sample.environment.*
import com.ksensor.sample.bluetooth.*
import com.ksensor.sample.system.*
import com.ksensor.sample.network.*
import com.ksensor.sample.interaction.*
import com.ksensor.sample.lifecycle.*

import com.ksensor.plugins.sensors.positioning.createPositioningPlugin
import com.ksensor.plugins.sensors.motion.createMotionPlugin
import com.ksensor.plugins.sensors.health.createHealthPlugin
import com.ksensor.plugins.sensors.environment.createEnvironmentPlugin
import com.ksensor.plugins.states.bluetooth.createBluetoothPlugin
import com.ksensor.plugins.states.system.createSystemPlugin
import com.ksensor.plugins.states.network.createNetworkPlugin
import com.ksensor.plugins.sensors.interaction.createInteractionPlugin
import com.ksensor.plugins.states.lifecycle.createLifecyclePlugin

sealed class Screen {
    object Catalog : Screen()
    data class PluginDetail(val pluginCatalog: PluginCatalog) : Screen()
}

data class PluginCatalog(
    val id: PluginId,
    val title: String,
    val description: String,
    val getOrCreatePlugin: () -> KSensorPlugin,
    val observers: List<ObserverSample>
)

data class ObserverSample(
    val name: String,
    val description: String,
    val content: @Composable () -> Unit
)

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Catalog) }

            val catalogItems = remember {
                listOf(
                    PluginCatalog(
                        id = PluginId.POSITIONING,
                        title = "Positioning Plugin",
                        description = "Provides Location, Heading, Orientation, Magnetometer, and Location Status.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.sensors.positioning.PositioningPlugin>(PluginId.POSITIONING)
                                ?: createPositioningPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Location", "Current latitude, longitude, and altitude") { LocationSample() },
                            ObserverSample("Heading", "Magnetic and true heading details") { HeadingSample() },
                            ObserverSample("Orientation", "Device orientation angles") { OrientationSample() },
                            ObserverSample("Magnetometer", "Magnetic field strength across 3 axes") { MagnetometerSample() },
                            ObserverSample("Location Status", "GPS / Location provider status") { LocationStatusSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.MOTION,
                        title = "Motion Plugin",
                        description = "Detects motion, accelerometer, gyroscope, and step counter.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.sensors.motion.MotionPlugin>(PluginId.MOTION)
                                ?: createMotionPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Motion Detector", "Detects device motion and activity state") { MotionSample() },
                            ObserverSample("Accelerometer", "Linear acceleration across 3 axes") { AccelerometerSample() },
                            ObserverSample("Gyroscope", "Rotation rate across 3 axes") { GyroscopeSample() },
                            ObserverSample("Step Counter", "Step count measurement") { StepCounterSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.HEALTH,
                        title = "Health Plugin",
                        description = "Measures heart rate via sensors or Camera PPG.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.sensors.health.HealthPlugin>(PluginId.HEALTH)
                                ?: createHealthPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Heart Rate", "Monitor heart rate and camera PPG state") { HealthSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.ENVIRONMENT,
                        title = "Environment Plugin",
                        description = "Measures noise, barometer, light, and proximity.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.sensors.environment.EnvironmentPlugin>(PluginId.ENVIRONMENT)
                                ?: createEnvironmentPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Noise Meter", "Measures ambient sound levels in dB") { NoiseSample() },
                            ObserverSample("Barometer", "Atmospheric pressure measurement") { BarometerSample() },
                            ObserverSample("Light Sensor", "Illuminance in lux") { LightSample() },
                            ObserverSample("Proximity", "Object distance and nearness") { ProximitySample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.BLUETOOTH,
                        title = "Bluetooth Plugin",
                        description = "Monitors connected, discovered, and audio Bluetooth devices.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.states.bluetooth.BluetoothPlugin>(PluginId.BLUETOOTH)
                                ?: createBluetoothPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Bluetooth Status", "Observe connected and discovered Bluetooth devices") { BluetoothSample() },
                            ObserverSample("Audio Devices", "Connected Bluetooth audio devices") { AudioDevicesSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.SYSTEM,
                        title = "System Plugin",
                        description = "Monitors brightness, storage, resources, battery, volume, locale, screen state, lock, and power save.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.states.system.SystemPlugin>(PluginId.SYSTEM)
                                ?: createSystemPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Brightness", "Screen brightness status and level") { BrightnessSample() },
                            ObserverSample("Storage", "Device storage capacity and usage") { StorageSample() },
                            ObserverSample("Resources", "CPU usage, load average, and memory status") { ResourcesSample() },
                            ObserverSample("Battery", "Battery level, charging state, health, and temp") { BatterySample() },
                            ObserverSample("Volume", "Audio volume levels") { VolumeSample() },
                            ObserverSample("Locale", "System locale and language settings") { LocaleSample() },
                            ObserverSample("Screen State", "Screen on/off status") { ScreenStateSample() },
                            ObserverSample("Device Lock", "Device lock status") { LockSample() },
                            ObserverSample("Power Save", "Power save mode status") { PowerSaveSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.NETWORK,
                        title = "Network Plugin",
                        description = "Monitors connectivity, active network, and call state.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.states.network.NetworkPlugin>(PluginId.NETWORK)
                                ?: createNetworkPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Call State", "Monitor telephony call state and activity") { CallStateSample() },
                            ObserverSample("Connectivity", "Network connectivity status") { ConnectivitySample() },
                            ObserverSample("Active Network", "Active network type and details") { ActiveNetworkSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.INTERACTION,
                        title = "Interaction Plugin",
                        description = "Monitors touch gestures and scroll position.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.sensors.interaction.InteractionPlugin>(PluginId.INTERACTION)
                                ?: createInteractionPlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("Scroll Monitor", "Observe user scroll position and activity") { ScrollSample() },
                            ObserverSample("Touch Gestures", "Touch gesture events") { TouchGesturesSample() }
                        )
                    ),
                    PluginCatalog(
                        id = PluginId.LIFECYCLE,
                        title = "Lifecycle Plugin",
                        description = "Monitors application visibility and lifecycle state.",
                        getOrCreatePlugin = {
                            KSensor.get<com.ksensor.plugins.states.lifecycle.LifecyclePlugin>(PluginId.LIFECYCLE)
                                ?: createLifecyclePlugin().also { KSensor.register(it) }
                        },
                        observers = listOf(
                            ObserverSample("App Visibility", "Observe foreground/background app visibility") { AppVisibilitySample() }
                        )
                    )
                )
            }

            when (val screen = currentScreen) {
                is Screen.Catalog -> {
                    CatalogScreen(catalogItems) { item ->
                        currentScreen = Screen.PluginDetail(item)
                    }
                }
                is Screen.PluginDetail -> {
                    PluginDetailScreen(screen.pluginCatalog) {
                        currentScreen = Screen.Catalog
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogScreen(items: List<PluginCatalog>, onSelectPlugin: (PluginCatalog) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KSensor Plugin Catalog") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Select a Plugin to test its sensors and state observers:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(items) { catalogItem ->
                Card(
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPlugin(catalogItem) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(catalogItem.title, style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(catalogItem.description, style = MaterialTheme.typography.body2)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Observers: ${catalogItem.observers.joinToString { it.name }}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PluginDetailScreen(pluginCatalog: PluginCatalog, onBack: () -> Unit) {
    val plugin = remember(pluginCatalog) { pluginCatalog.getOrCreatePlugin() }
    val requiredPermissions = plugin.requiredPermissions

    var grantedPermissions by remember(pluginCatalog) {
        mutableStateOf(requiredPermissions.filter { KSensor.permissionHandler.hasPermission(it) }.toSet())
    }

    val remainingPermissions = requiredPermissions.filter { it !in grantedPermissions }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pluginCatalog.title) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = MaterialTheme.colors.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (remainingPermissions.isNotEmpty()) {
                val nextPermission = remainingPermissions.first()
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Permission Required",
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This plugin requires $nextPermission permission to function.",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { /* Handled by AskPermission */ }) {
                        Text("Grant Permission")
                    }

                    KSensor.permissionHandler.AskPermission(nextPermission) { status ->
                        if (status == PermissionStatus.GRANTED) {
                            grantedPermissions = grantedPermissions + nextPermission
                        }
                    }
                }
            } else {
                // Permissions granted - show observers selection/testing UI
                var selectedObserverIndex by remember { mutableStateOf(0) }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (pluginCatalog.observers.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedObserverIndex,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pluginCatalog.observers.forEachIndexed { index, observer ->
                                Tab(
                                    selected = selectedObserverIndex == index,
                                    onClick = { selectedObserverIndex = index },
                                    text = { Text(observer.name) }
                                )
                            }
                        }
                    } else if (pluginCatalog.observers.isNotEmpty()) {
                        Surface(
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Active Observer: ${pluginCatalog.observers[0].name}",
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        if (pluginCatalog.observers.isNotEmpty()) {
                            pluginCatalog.observers[selectedObserverIndex].content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
expect fun NativeScrollableContent(modifier: Modifier)
