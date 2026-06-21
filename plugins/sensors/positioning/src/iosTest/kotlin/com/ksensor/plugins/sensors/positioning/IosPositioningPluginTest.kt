package com.ksensor.plugins.sensors.positioning

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosPositioningPluginTest {

    @Test
    fun testLocation() = runBlocking {
        val plugin = IosPositioningPlugin()
        withTimeoutOrNull(2000) {
            plugin.location().first()
        }
    }

    @Test
    fun testMagnetometer() = runBlocking {
        val plugin = IosPositioningPlugin()
        withTimeoutOrNull(2000) {
            plugin.magnetometer().first()
        }
    }

    @Test
    fun testOrientation() = runBlocking {
        val plugin = IosPositioningPlugin()
        withTimeoutOrNull(2000) {
            plugin.orientation().first()
        }
    }
}
