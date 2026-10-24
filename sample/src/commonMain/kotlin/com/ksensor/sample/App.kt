package com.ksensor.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ksensor.core.TargetPlatform
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
    object ManifestGenerator : Screen()
}

enum class CatalogCategory { SENSORS, STATES }

data class PluginCatalog(
    val id: PluginId,
    val title: String,
    val description: String,
    val category: CatalogCategory,
    val capabilities: List<String>,
    val dataTypeExamples: List<String>,
    val permissionNotes: String,
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
                        category = CatalogCategory.SENSORS,
                        capabilities = listOf("Location", "Heading", "Orientation", "Magnetometer"),
                        dataTypeExamples = listOf(
                            "Location(latitude=37.42, longitude=-122.08, altitude=10.0)",
                            "Heading(magneticHeading=45.0, trueHeading=42.0)",
                            "Magnetometer(values=Vector3(x=12.5, y=-5.2, z=45.1))"
                        ),
                        permissionNotes = "Requires ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION on Android and WhenInUse authorization on iOS.",
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
                        category = CatalogCategory.SENSORS,
                        capabilities = listOf("Motion", "Accelerometer", "Gyroscope", "StepCounter"),
                        dataTypeExamples = listOf(
                            "MotionDetector(type=WALKING)",
                            "Accelerometer(values=Vector3(x=0.0, y=9.8, z=0.1))",
                            "StepCounter(steps=1250)"
                        ),
                        permissionNotes = "Requires ACTIVITY_RECOGNITION permission on Android for step counting and motion detection.",
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
                        category = CatalogCategory.SENSORS,
                        capabilities = listOf("HeartRate", "CameraPPG"),
                        dataTypeExamples = listOf(
                            "HeartRate(heartRate=72.0f, source=CAMERA_PPG, confidence=0.85f)"
                        ),
                        permissionNotes = "Requires BODY_SENSORS and CAMERA permissions for optical PPG heart rate monitoring.",
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
                        category = CatalogCategory.SENSORS,
                        capabilities = listOf("Noise", "Barometer", "Light", "Proximity"),
                        dataTypeExamples = listOf(
                            "Noise(dB=45.5f, isLoud=false)",
                            "Barometer(pressure=1013.25f)",
                            "LightIlluminance(illuminance=350.0f)"
                        ),
                        permissionNotes = "Noise monitoring requires RECORD_AUDIO permission. Barometer/Light/Proximity require no runtime permissions.",
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
                        category = CatalogCategory.STATES,
                        capabilities = listOf("Bluetooth", "Connections", "Discoveries", "Audio"),
                        dataTypeExamples = listOf(
                            "BleConnectionStatus(connectedDevices=[BleDevice(id=\"00:11...\", name=\"Headphones\")])"
                        ),
                        permissionNotes = "Requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions (API 31+) on Android.",
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
                        category = CatalogCategory.STATES,
                        capabilities = listOf("Brightness", "Storage", "Resources", "Battery", "Volume", "Locale", "Screen", "Lock", "PowerSave"),
                        dataTypeExamples = listOf(
                            "BrightnessStatus(screenBrightness=80)",
                            "BatteryStatus(levelPercent=85, chargingState=CHARGING)",
                            "StorageStatus(totalBytes=..., freeBytes=...)"
                        ),
                        permissionNotes = "No runtime permissions required for system states.",
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
                        category = CatalogCategory.STATES,
                        capabilities = listOf("Network", "CallState", "Connectivity"),
                        dataTypeExamples = listOf(
                            "CallStateStatus(state=IDLE, isInCall=false)",
                            "ConnectivityStatus(isConnected=true)"
                        ),
                        permissionNotes = "Call State monitoring requires READ_PHONE_STATE permission on Android.",
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
                        category = CatalogCategory.SENSORS,
                        capabilities = listOf("Scroll", "TouchGestures"),
                        dataTypeExamples = listOf(
                            "ScrollStatus(horizontalPosition=0.0f, verticalPosition=120.0f, isScrolling=true)",
                            "TouchGestures(x=100.0f, y=250.0f, type=TAP)"
                        ),
                        permissionNotes = "No special runtime permissions required.",
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
                        category = CatalogCategory.STATES,
                        capabilities = listOf("Lifecycle", "AppVisibility"),
                        dataTypeExamples = listOf(
                            "AppVisibilityStatus(isAppVisible=true)"
                        ),
                        permissionNotes = "No runtime permissions required.",
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
                    CatalogScreen(
                        items = catalogItems,
                        onSelectPlugin = { item -> currentScreen = Screen.PluginDetail(item) },
                        onOpenManifestGenerator = { currentScreen = Screen.ManifestGenerator }
                    )
                }
                is Screen.PluginDetail -> {
                    PluginDetailScreen(screen.pluginCatalog) {
                        currentScreen = Screen.Catalog
                    }
                }
                is Screen.ManifestGenerator -> {
                    ManifestGeneratorScreen(catalogItems) {
                        currentScreen = Screen.Catalog
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogScreen(
    items: List<PluginCatalog>,
    onSelectPlugin: (PluginCatalog) -> Unit,
    onOpenManifestGenerator: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CatalogCategory?>(null) }
    var selectedCapability by remember { mutableStateOf<String?>(null) }

    val allCapabilities = remember(items) { items.flatMap { it.capabilities }.distinct() }

    val filteredItems = remember(items, searchQuery, selectedCategory, selectedCapability) {
        items.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            val matchesCapability = selectedCapability == null || item.capabilities.contains(selectedCapability)
            matchesSearch && matchesCategory && matchesCapability
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KSensor Plugin Catalog") },
                actions = {
                    TextButton(onClick = onOpenManifestGenerator) {
                        Text("Manifests", color = MaterialTheme.colors.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search plugins...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Categories") }
                )
                FilterChip(
                    selected = selectedCategory == CatalogCategory.SENSORS,
                    onClick = { selectedCategory = CatalogCategory.SENSORS },
                    label = { Text("Sensors") }
                )
                FilterChip(
                    selected = selectedCategory == CatalogCategory.STATES,
                    onClick = { selectedCategory = CatalogCategory.STATES },
                    label = { Text("States") }
                )
            }

            // Capability Filter Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCapability == null,
                        onClick = { selectedCapability = null },
                        label = { Text("All Capabilities") }
                    )
                }
                items(allCapabilities) { cap ->
                    FilterChip(
                        selected = selectedCapability == cap,
                        onClick = { selectedCapability = if (selectedCapability == cap) null else cap },
                        label = { Text(cap) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Catalog List
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems) { catalogItem ->
                    Card(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPlugin(catalogItem) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(catalogItem.title, style = MaterialTheme.typography.h6)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = catalogItem.category.name,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(catalogItem.description, style = MaterialTheme.typography.body2)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                catalogItem.capabilities.take(3).forEach { cap ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = cap,
                                            style = MaterialTheme.typography.caption,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
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
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        elevation = if (selected) 2.dp else 1.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
            ) {
                label()
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
                        Text("← Back", color = MaterialTheme.colors.onPrimary)
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
                // Permissions granted - show info (permission notes & data examples) + observers testing UI
                var selectedTab by remember { mutableStateOf(0) } // 0 = Live Test, 1 = Info & Docs

                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Live Test") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Info & Documentation") }
                        )
                    }

                    if (selectedTab == 1) {
                        // Info & Documentation Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Permission Notes", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(pluginCatalog.permissionNotes, style = MaterialTheme.typography.body1)
                                    }
                                }
                            }
                            item {
                                Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Generated Permission Manifests", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Android (AndroidManifest.xml):", style = MaterialTheme.typography.subtitle2)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = KSensor.permissionManifest(listOf(plugin)),
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("iOS (Info.plist):", style = MaterialTheme.typography.subtitle2)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = KSensor.iosInfoPlistEntries(listOf(plugin)),
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Data Type Examples", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        pluginCatalog.dataTypeExamples.forEach { example ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colors.surface,
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = example,
                                                    style = MaterialTheme.typography.body2,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Test Tab
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
    }
}

@Composable
fun ManifestGeneratorScreen(
    catalogItems: List<PluginCatalog>,
    onBack: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(TargetPlatform.ANDROID) }
    var includeOptional by remember { mutableStateOf(true) }
    var includeComments by remember { mutableStateOf(true) }
    var selectedPluginId by remember { mutableStateOf<PluginId?>(null) }

    val allPlugins = remember(catalogItems) {
        catalogItems.map { it.getOrCreatePlugin() }
    }

    val activePlugins = remember(allPlugins, selectedPluginId) {
        if (selectedPluginId == null) {
            allPlugins
        } else {
            allPlugins.filter { it.id == selectedPluginId }
        }
    }

    val generatedSnippet = remember(activePlugins, selectedPlatform, includeOptional, includeComments) {
        when (selectedPlatform) {
            TargetPlatform.ANDROID -> KSensor.permissionManifest(
                plugins = activePlugins,
                includeOptional = includeOptional,
                includeComments = includeComments
            )
            TargetPlatform.IOS -> KSensor.iosInfoPlistEntries(
                plugins = activePlugins,
                includeOptional = includeOptional,
                includeComments = includeComments
            )
            TargetPlatform.COMMON -> "Select Android or iOS platform to generate manifest entries."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manifest Generator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.h6)
                    }
                },
                actions = {
                    TextButton(onClick = { KSensor.printPermissionManifests(activePlugins) }) {
                        Text("Log", color = MaterialTheme.colors.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Platform Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPlatform == TargetPlatform.ANDROID,
                    onClick = { selectedPlatform = TargetPlatform.ANDROID },
                    label = { Text("AndroidManifest.xml") }
                )
                FilterChip(
                    selected = selectedPlatform == TargetPlatform.IOS,
                    onClick = { selectedPlatform = TargetPlatform.IOS },
                    label = { Text("iOS Info.plist") }
                )
            }

            // Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeOptional,
                        onCheckedChange = { includeOptional = it }
                    )
                    Text("Optional permissions", style = MaterialTheme.typography.body2)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeComments,
                        onCheckedChange = { includeComments = it }
                    )
                    Text("Comments", style = MaterialTheme.typography.body2)
                }
            }

            // Plugin Filter Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedPluginId == null,
                        onClick = { selectedPluginId = null },
                        label = { Text("All Plugins") }
                    )
                }
                items(catalogItems) { item ->
                    FilterChip(
                        selected = selectedPluginId == item.id,
                        onClick = { selectedPluginId = if (selectedPluginId == item.id) null else item.id },
                        label = { Text(item.title.replace(" Plugin", "")) }
                    )
                }
            }

            // Output Card
            Card(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedPlatform == TargetPlatform.ANDROID) "Generated AndroidManifest.xml:" else "Generated iOS Info.plist:",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = generatedSnippet,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
expect fun NativeScrollableContent(modifier: Modifier)
