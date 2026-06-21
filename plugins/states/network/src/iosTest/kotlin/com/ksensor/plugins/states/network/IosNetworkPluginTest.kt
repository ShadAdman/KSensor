package com.ksensor.plugins.states.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosNetworkPluginTest {

    @Test
    fun testConnectivity() = runBlocking {
        val plugin = IosNetworkPlugin()
        withTimeoutOrNull(2000) {
            plugin.connectivity().observe().first()
        }
    }

    @Test
    fun testActiveNetwork() = runBlocking {
        val plugin = IosNetworkPlugin()
        withTimeoutOrNull(2000) {
            plugin.activeNetwork().observe().first()
        }
    }
}
