package com.ksensor.plugins.sensors.fused.activity

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActivityClassificationPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createActivityClassificationPlugin()
        assertEquals(PluginId.FUSED_ACTIVITY, plugin.id)
        assertTrue(plugin.requiredPermissions.isNotEmpty())
    }

    @Test
    fun testObserveEmitsFusedActivity() = runTest {
        val plugin = createActivityClassificationPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.MOTION_DETECTOR })
    }
}
