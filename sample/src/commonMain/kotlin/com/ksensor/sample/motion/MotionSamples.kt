package com.ksensor.sample.motion

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ksensor.core.KSensor
import com.ksensor.core.model.PluginId
import com.ksensor.plugins.sensors.motion.MotionPlugin
import com.ksensor.plugins.sensors.motion.createMotionPlugin
import com.ksensor.sample.toTwoDecimalString

@Composable
fun MotionSample() {
    val plugin = remember {
        KSensor.get<MotionPlugin>(PluginId.MOTION)
            ?: createMotionPlugin().also { KSensor.register(it) }
    }

    val motion by plugin.motionDetector().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Motion Detector Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (motion != null) {
            Text("Motion Type: ${motion?.data?.type}")
        } else {
            Text("Waiting for motion data...")
        }
    }
}

@Composable
fun AccelerometerSample() {
    val plugin = remember {
        KSensor.get<MotionPlugin>(PluginId.MOTION)
            ?: createMotionPlugin().also { KSensor.register(it) }
    }

    val accel by plugin.accelerometer().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Accelerometer Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (accel != null) {
            val data = accel?.data
            Text("X: ${data?.values?.x?.toDouble()?.toTwoDecimalString() ?: "N/A"} m/s²")
            Text("Y: ${data?.values?.y?.toDouble()?.toTwoDecimalString() ?: "N/A"} m/s²")
            Text("Z: ${data?.values?.z?.toDouble()?.toTwoDecimalString() ?: "N/A"} m/s²")
        } else {
            Text("Waiting for accelerometer data...")
        }
    }
}

@Composable
fun GyroscopeSample() {
    val plugin = remember {
        KSensor.get<MotionPlugin>(PluginId.MOTION)
            ?: createMotionPlugin().also { KSensor.register(it) }
    }

    val gyro by plugin.gyroscope().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gyroscope Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (gyro != null) {
            val data = gyro?.data
            Text("X: ${data?.values?.x?.toDouble()?.toTwoDecimalString() ?: "N/A"} rad/s")
            Text("Y: ${data?.values?.y?.toDouble()?.toTwoDecimalString() ?: "N/A"} rad/s")
            Text("Z: ${data?.values?.z?.toDouble()?.toTwoDecimalString() ?: "N/A"} rad/s")
        } else {
            Text("Waiting for gyroscope data...")
        }
    }
}

@Composable
fun StepCounterSample() {
    val plugin = remember {
        KSensor.get<MotionPlugin>(PluginId.MOTION)
            ?: createMotionPlugin().also { KSensor.register(it) }
    }

    val steps by plugin.stepCounter().collectAsState(null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Step Counter Sample", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (steps != null) {
            Text("Steps: ${steps?.data?.steps ?: 0}")
        } else {
            Text("Waiting for step data...")
        }
    }
}
