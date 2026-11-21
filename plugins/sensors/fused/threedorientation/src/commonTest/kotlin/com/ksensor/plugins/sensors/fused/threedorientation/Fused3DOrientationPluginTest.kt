package com.ksensor.plugins.sensors.fused.threedorientation

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Fused3DOrientationPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createFused3DOrientationPlugin()
        assertEquals(PluginId.FUSED_3D_ORIENTATION, plugin.id)
        assertTrue(plugin.requiredPermissions.isEmpty())
    }

    @Test
    fun testObserveEmitsFused3DOrientation() = runTest {
        val plugin = createFused3DOrientationPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.ACCELEROMETER })
    }
}
