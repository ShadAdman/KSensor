package com.ksensor.plugins.sensors.fused.surrounding

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SurroundingPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createSurroundingPlugin()
        assertEquals(PluginId.FUSED_SURROUNDING, plugin.id)
        assertTrue(plugin.requiredPermissions.isNotEmpty())
    }

    @Test
    fun testObserveEmitsFusedSurrounding() = runTest {
        val plugin = createSurroundingPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.LIGHT })
    }
}
