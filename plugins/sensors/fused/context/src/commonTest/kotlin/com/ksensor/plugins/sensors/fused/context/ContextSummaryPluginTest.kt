package com.ksensor.plugins.sensors.fused.context

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContextSummaryPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createContextSummaryPlugin()
        assertEquals(PluginId.FUSED_CONTEXT, plugin.id)
        assertTrue(plugin.requiredPermissions.isNotEmpty())
    }

    @Test
    fun testObserveEmitsFusedContextSummary() = runTest {
        val plugin = createContextSummaryPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.LIGHT })
    }
}
