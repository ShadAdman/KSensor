package com.ksensor.plugins.sensors.interaction

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidInteractionPluginTest {

    @Test
    fun testTouchGestures() = runBlocking {
        val plugin = AndroidInteractionPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.touchGestures().first()
        }
    }
}
