package com.ksensor.plugins.states.lifecycle

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosLifecyclePluginTest {

    @Test
    fun testAppVisibility() = runBlocking {
        val plugin = IosLifecyclePlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.appVisibility().observe().first()
        }
    }
}
