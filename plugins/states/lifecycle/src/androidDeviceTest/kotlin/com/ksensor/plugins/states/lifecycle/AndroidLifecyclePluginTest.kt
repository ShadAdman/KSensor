package com.ksensor.plugins.states.lifecycle

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidLifecyclePluginTest {

    @Test
    fun testAppVisibility() = runBlocking {
        val plugin = AndroidLifecyclePlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.appVisibility().observe().first()
        }
        assertNotNull(data)
    }
}
