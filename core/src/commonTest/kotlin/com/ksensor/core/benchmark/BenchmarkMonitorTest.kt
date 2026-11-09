package com.ksensor.core.benchmark

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BenchmarkMonitorTest {

    @BeforeTest
    fun setUp() {
        BenchmarkMonitor.reset()
    }

    @Test
    fun testRecordEventUpdatesMetrics() {
        val now = 1000L
        BenchmarkMonitor.recordEvent(
            pluginId = PluginId.ACCELEROMETER,
            eventTimestamp = now - 50L,
            receiveTimestamp = now
        )

        val snapshot = BenchmarkMonitor.getSnapshot()
        assertEquals(1L, snapshot.totalEventsCount)
        val pluginMetrics = snapshot.pluginMetrics[PluginId.ACCELEROMETER]
        assertTrue(pluginMetrics != null)
        assertEquals(1L, pluginMetrics.eventCount)
        assertEquals(50.0, pluginMetrics.averageLatencyMs)
    }

    @Test
    fun testFlowBenchmarkExtension() = runTest {
        val flow = flowOf(
            KSensorResponse("data1", timestamp = 100L),
            KSensorResponse("data2", timestamp = 105L)
        ).benchmark(PluginId.GYROSCOPE)

        val items = flow.toList()
        assertEquals(2, items.size)

        val snapshot = BenchmarkMonitor.getSnapshot()
        assertEquals(2L, snapshot.totalEventsCount)
        val gyroMetrics = snapshot.pluginMetrics[PluginId.GYROSCOPE]
        assertTrue(gyroMetrics != null)
        assertEquals(2L, gyroMetrics.eventCount)
    }

    @Test
    fun testResetClearsMetrics() {
        BenchmarkMonitor.recordEvent(PluginId.BAROMETER)
        assertTrue(BenchmarkMonitor.getSnapshot().totalEventsCount > 0)

        BenchmarkMonitor.reset()
        assertEquals(0L, BenchmarkMonitor.getSnapshot().totalEventsCount)
    }
}
