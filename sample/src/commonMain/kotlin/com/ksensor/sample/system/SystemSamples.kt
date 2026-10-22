package com.ksensor.sample.system

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.states.system.SystemPlugin
import com.ksensor.plugins.states.system.createSystemPlugin
import com.ksensor.sample.toTwoDecimalString
import kotlin.time.Duration.Companion.seconds

@Composable
fun BrightnessSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val brightnessResponse by plugin.brightness().observe().collectAsState(null)
    val brightness = brightnessResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Brightness Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (brightness != null) {
            Text("Screen Brightness: ${brightness.screenBrightness}%")

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = brightness.screenBrightness.toFloat() / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Try changing your system brightness to see it update.", style = MaterialTheme.typography.caption)
        } else {
            Text("Loading brightness data...")
        }
    }
}

@Composable
fun StorageSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val storageResponse by plugin.storage(interval = 2.seconds).observe().collectAsState(null)
    val storage = storageResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Storage Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (storage != null) {
            val totalGB = storage.totalBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGB = storage.usedBytes / (1024.0 * 1024.0 * 1024.0)
            val freeGB = storage.freeBytes / (1024.0 * 1024.0 * 1024.0)

            Text("Total Storage: ${totalGB.toTwoDecimalString()} GB")
            Text("Used Storage: ${usedGB.toTwoDecimalString()} GB")
            Text("Free Storage: ${freeGB.toTwoDecimalString()} GB")

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = if (storage.totalBytes > 0) storage.usedBytes.toFloat() / storage.totalBytes else 0f,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        } else {
            Text("Loading storage data...")
        }
    }
}

@Composable
fun ResourcesSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val resourcesResponse by plugin.resources(interval = 1.seconds).observe().collectAsState(null)
    val resources = resourcesResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Device Resources Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (resources != null) {
            Text("CPU Usage (Total): ${resources.cpuUsagePercent.toTwoDecimalString()}%")
            Text("App CPU Usage: ${resources.appCpuUsagePercent.toTwoDecimalString()}%")

            Spacer(modifier = Modifier.height(16.dp))

            Text("System Load Average:", style = MaterialTheme.typography.h6)
            resources.systemLoadAverage.forEachIndexed { index, load ->
                val time = when(index) {
                    0 -> "1 min"
                    1 -> "5 min"
                    else -> "15 min"
                }
                Text("  • $time: ${load.toTwoDecimalString()}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Memory Status:", style = MaterialTheme.typography.h6)
            val totalMB = resources.memoryUsage.totalBytes / (1024.0 * 1024.0)
            val usedMB = resources.memoryUsage.usedBytes / (1024.0 * 1024.0)
            val freeMB = resources.memoryUsage.freeBytes / (1024.0 * 1024.0)

            Text("  • Total: ${totalMB.toTwoDecimalString()} MB")
            Text("  • Used: ${usedMB.toTwoDecimalString()} MB")
            Text("  • Free: ${freeMB.toTwoDecimalString()} MB")
            Text("  • Pressure: ${resources.memoryPressure.name}")

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (resources.memoryUsage.totalBytes > 0) usedMB.toFloat() / totalMB.toFloat() else 0f,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        } else {
            Text("Loading resources data...")
        }
    }
}

@Composable
fun BatterySample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val battery by plugin.battery().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Battery Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (battery != null) {
            val data = battery?.data
            Text("Level: ${data?.levelPercent ?: 0}%")
            Text("Charging State: ${data?.chargingState}")
            Text("Health: ${data?.health}")
            Text("Temperature: ${data?.temperatureC?.toDouble()?.toTwoDecimalString() ?: "N/A"} °C")
        } else {
            Text("Loading battery status...")
        }
    }
}

@Composable
fun VolumeSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val volume by plugin.volume().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Volume Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (volume != null) {
            val data = volume?.data
            Text("Volume Percentage: ${data?.volumePercentage}%")
        } else {
            Text("Loading volume status...")
        }
    }
}

@Composable
fun LocaleSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val locale by plugin.locale().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Locale Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (locale != null) {
            val data = locale?.data
            Text("Language: ${data?.languageCode}")
            Text("Country: ${data?.countryCode}")
            Text("Display Name: ${data?.displayName}")
            Text("Is RTL: ${data?.isRTL == true}")
        } else {
            Text("Loading locale status...")
        }
    }
}

@Composable
fun ScreenStateSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val screen by plugin.screen().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Screen State Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (screen != null) {
            val data = screen?.data
            Text("Is Screen On: ${data?.isScreenOn == true}")
        } else {
            Text("Loading screen state...")
        }
    }
}

@Composable
fun LockSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val lock by plugin.lock().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Device Lock Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (lock != null) {
            val data = lock?.data
            Text("Is Locked: ${data?.isDeviceLocked == true}")
        } else {
            Text("Loading lock status...")
        }
    }
}

@Composable
fun PowerSaveSample() {
    val plugin = remember {
        KSensor.get<SystemPlugin>(PluginId.SYSTEM)
            ?: createSystemPlugin().also { KSensor.register(it) }
    }

    val powerSave by plugin.powerSave().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Power Save Status Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (powerSave != null) {
            val data = powerSave?.data
            Text("Is Power Save Enabled: ${data?.isPowerSaveMode == true}")
        } else {
            Text("Loading power save status...")
        }
    }
}
