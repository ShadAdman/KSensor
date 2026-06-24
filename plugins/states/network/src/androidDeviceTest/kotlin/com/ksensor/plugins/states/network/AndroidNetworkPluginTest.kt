package com.ksensor.plugins.states.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidNetworkPluginTest {

    @Test
    fun testConnectivity() = runBlocking {
        val plugin = AndroidNetworkPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.connectivity().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testActiveNetwork() = runBlocking {
        val plugin = AndroidNetworkPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.activeNetwork().observe().first()
        }
        assertNotNull(data)
    }
}
