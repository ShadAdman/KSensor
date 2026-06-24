package com.ksensor.plugins.sensors.motion

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidMotionPluginTest {

    @Test
    fun testAccelerometer() = runBlocking {
        val plugin = AndroidMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.accelerometer().first()
        }
    }

    @Test
    fun testGyroscope() = runBlocking {
        val plugin = AndroidMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.gyroscope().first()
        }
    }

    @Test
    fun testStepCounter() = runBlocking {
        val plugin = AndroidMotionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.stepCounter().first()
        }
    }
}
