package com.ksensor.plugins.sensors.interaction

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class IosInteractionPluginTest {

    @Test
    fun testTouchGestures() = runBlocking {
        val plugin = IosInteractionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.touchGestures().first()
        }
    }
}
