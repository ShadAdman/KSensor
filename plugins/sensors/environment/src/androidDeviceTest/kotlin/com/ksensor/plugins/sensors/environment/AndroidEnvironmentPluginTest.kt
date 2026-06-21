package com.ksensor.plugins.sensors.environment

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidEnvironmentPluginTest {

    @Test
    fun testBarometer() = runBlocking {
        val plugin = AndroidEnvironmentPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.barometer().first()
        }
    }

    @Test
    fun testLight() = runBlocking {
        val plugin = AndroidEnvironmentPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.light().first()
        }
    }

    @Test
    fun testProximity() = runBlocking {
        val plugin = AndroidEnvironmentPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.proximity().first()
        }
    }
}
