package com.ksensor.sample.interaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.interaction.InteractionPlugin
import com.ksensor.plugins.sensors.interaction.createInteractionPlugin
import com.ksensor.sample.NativeScrollableContent

@Composable
fun ScrollSample() {
    val plugin = remember {
        KSensor.get<InteractionPlugin>(PluginId.INTERACTION)
            ?: createInteractionPlugin().also { KSensor.register(it) }
    }

    val scrollResponse by plugin.scroll().observe().collectAsState(null)
    val scroll = scrollResponse?.data

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Scroll Status Sample (via InteractionPlugin)", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (scroll != null) {
            Text("Horizontal Position: ${scroll.horizontalPosition}")
            Text("Vertical Position: ${scroll.verticalPosition}")
            Text("Is Scrolling: ${scroll.isScrolling}")
        } else {
            Text("Scroll the content below...")
        }

        Spacer(modifier = Modifier.height(16.dp))

        NativeScrollableContent(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun TouchGesturesSample() {
    val plugin = remember {
        KSensor.get<InteractionPlugin>(PluginId.INTERACTION)
            ?: createInteractionPlugin().also { KSensor.register(it) }
    }

    val gesture by plugin.touchGestures().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Touch Gestures Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (gesture != null) {
            val data = gesture?.data
            Text("Gesture Type: ${data?.type}")
            Text("X: ${data?.x}, Y: ${data?.y}")
        } else {
            Text("Perform touch gestures on screen...")
        }
    }
}
