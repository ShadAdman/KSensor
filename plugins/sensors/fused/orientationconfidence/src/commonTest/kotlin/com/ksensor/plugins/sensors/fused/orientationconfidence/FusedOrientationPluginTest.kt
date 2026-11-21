package com.ksensor.plugins.sensors.fused.orientationconfidence

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FusedOrientationPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createFusedOrientationPlugin()
        assertEquals(PluginId.FUSED_ORIENTATION, plugin.id)
        assertTrue(plugin.requiredPermissions.isEmpty())
    }

    @Test
    fun testObserveEmitsFusedOrientation() = runTest {
        val plugin = createFusedOrientationPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.ACCELEROMETER })
    }
}
