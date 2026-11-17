package com.ksensor.plugins.sensors.fused

import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FusedSensorPluginTest {

    @Test
    fun testParentPluginId() {
        val plugin = createFusedSensorPlugin()
        assertEquals(PluginId.FUSED_SENSOR, plugin.id)
    }

    @Test
    fun testAllFusedFlows() = runTest {
        val plugin = createFusedSensorPlugin()

        val activity = plugin.activityClassification().first()
        assertNotNull(activity.data)

        val posture = plugin.postureEstimation().first()
        assertNotNull(posture.data)

        val orientation = plugin.fusedOrientation().first()
        assertNotNull(orientation.data)

        val context = plugin.contextSummary().first()
        assertNotNull(context.data)
    }
}
