package com.ksensor.plugins.sensors.motion

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosMotionPluginTest {

    @Test
    fun testAccelerometer() = runBlocking {
        val plugin = IosMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.accelerometer().first()
        }
    }

    @Test
    fun testGyroscope() = runBlocking {
        val plugin = IosMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.gyroscope().first()
        }
    }

    @Test
    fun testStepCounter() = runBlocking {
        val plugin = IosMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.stepCounter().first()
        }
    }
}
