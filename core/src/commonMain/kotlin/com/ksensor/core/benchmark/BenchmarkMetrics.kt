package com.ksensor.core.benchmark

import com.ksensor.core.model.PluginId

/**
 * Performance metrics for a single KSensor plugin.
 */
data class PluginBenchmarkMetrics(
    val pluginId: PluginId,
    val eventCount: Long = 0L,
    val eventsPerSecond: Double = 0.0,
    val averageLatencyMs: Double = 0.0,
    val minLatencyMs: Double = 0.0,
    val maxLatencyMs: Double = 0.0,
    val lastEventTimestamp: Long = 0L
)

/**
 * Global live benchmark report across all active KSensor plugins.
 */
data class BenchmarkReport(
    val activePluginsCount: Int = 0,
    val totalEventsCount: Long = 0L,
    val totalEventsPerSecond: Double = 0.0,
    val averageLatencyMs: Double = 0.0,
    val maxLatencyMs: Double = 0.0,
    val memoryUsageMb: Double = 0.0,
    val pluginMetrics: Map<PluginId, PluginBenchmarkMetrics> = emptyMap(),
    val timestamp: Long = 0L
)
