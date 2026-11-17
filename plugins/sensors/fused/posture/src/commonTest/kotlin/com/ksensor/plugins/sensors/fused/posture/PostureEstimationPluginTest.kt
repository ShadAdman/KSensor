package com.ksensor.plugins.sensors.fused.posture

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PostureEstimationPluginTest {

    @Test
    fun testPluginIdAndPermissions() {
        val plugin = createPostureEstimationPlugin()
        assertEquals(PluginId.FUSED_POSTURE, plugin.id)
        assertTrue(plugin.requiredPermissions.isEmpty())
    }

    @Test
    fun testObserveEmitsFusedPosture() = runTest {
        val plugin = createPostureEstimationPlugin()
        val response = plugin.observe().first()
        assertNotNull(response.data)
        assertNotNull(response.data.confidence)
        assertNotNull(response.data.sources)
        assertTrue(response.data.sources.any { it.pluginId == PluginId.ACCELEROMETER })
    }
}
