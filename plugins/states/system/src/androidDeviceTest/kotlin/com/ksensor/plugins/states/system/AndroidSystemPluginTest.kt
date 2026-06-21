package com.ksensor.plugins.states.system

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidSystemPluginTest {

    @Test
    fun testBattery() = runBlocking {
        val plugin = AndroidSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.battery().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testVolume() = runBlocking {
        val plugin = AndroidSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.volume().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testLocale() = runBlocking {
        val plugin = AndroidSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.locale().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testScreen() = runBlocking {
        val plugin = AndroidSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.screen().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testLock() = runBlocking {
        val plugin = AndroidSystemPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.lock().observe().first()
        }
        assertNotNull(data)
    }
}
