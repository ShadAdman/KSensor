package com.ksensor.plugins.sensors.environment

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosEnvironmentPluginTest {

    @Test
    fun testBarometer() {
        runBlocking {
            val plugin = IosEnvironmentPlugin()
            val data = withTimeoutOrNull(2000) {
                plugin.barometer().first()
            }
        }
    }

    @Test
    fun testLight() = runBlocking {
        val plugin = IosEnvironmentPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.light().first()
        }
    }

    @Test
    fun testProximity() = runBlocking {
        val plugin = IosEnvironmentPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.proximity().first()
        }
    }
}
