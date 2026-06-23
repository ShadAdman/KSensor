package com.ksensor.plugins.states.bluetooth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosBluetoothPluginTest {

    @Test
    fun testConnections() = runBlocking {
        val plugin = IosBluetoothPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.connections().observe().first()
        }
    }

    @Test
    fun testDiscoveries() = runBlocking {
        val plugin = IosBluetoothPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.discoveries().observe().first()
        }
    }
}
