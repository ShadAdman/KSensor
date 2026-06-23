package com.ksensor.plugins.states.system

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosSystemPluginTest {

    @Test
    fun testBattery() = runBlocking {
        val plugin = IosSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.battery().observe().first()
        }
    }

    @Test
    fun testVolume() = runBlocking {
        val plugin = IosSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.volume().observe().first()
        }
    }

    @Test
    fun testLocale() = runBlocking {
        val plugin = IosSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.locale().observe().first()
        }
    }

    @Test
    fun testScreen() = runBlocking {
        val plugin = IosSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.screen().observe().first()
        }
    }

    @Test
    fun testLock() = runBlocking {
        val plugin = IosSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.lock().observe().first()
        }
    }
}
