package com.ksensor.sample.lifecycle

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.states.lifecycle.LifecyclePlugin
import com.ksensor.plugins.states.lifecycle.createLifecyclePlugin

@Composable
fun AppVisibilitySample() {
    val plugin = remember {
        KSensor.get<LifecyclePlugin>(PluginId.LIFECYCLE)
            ?: createLifecyclePlugin().also { KSensor.register(it) }
    }

    val visibility by plugin.appVisibility().observe().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("App Visibility Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (visibility != null) {
            val data = visibility?.data
            Text("Is App Visible: ${data?.isAppVisible == true}")
        } else {
            Text("Loading app visibility state...")
        }
    }
}
