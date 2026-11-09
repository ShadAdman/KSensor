package com.ksensor.core.benchmark

import com.ksensor.core.currentTimestamp
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

/**
 * Thread-safe live telemetry collector and benchmark monitor for KSensor.
 */
object BenchmarkMonitor {

    private class PluginTracker(val pluginId: PluginId) {
        var eventCount: Long = 0L
        val eventTimestamps = mutableListOf<Long>()
        val latenciesMs = mutableListOf<Double>()
        var minLatencyMs: Double = Double.MAX_VALUE
        var maxLatencyMs: Double = 0.0
        var sumLatencyMs: Double = 0.0
        var lastEventTimestamp: Long = 0L

        fun record(eventTimestamp: Long, receiveTimestamp: Long) {
            val latency = (receiveTimestamp - eventTimestamp).toDouble().coerceAtLeast(0.0)
            eventCount++
            lastEventTimestamp = receiveTimestamp
            eventTimestamps.add(receiveTimestamp)
            latenciesMs.add(latency)

            if (latency < minLatencyMs) minLatencyMs = latency
            if (latency > maxLatencyMs) maxLatencyMs = latency
            sumLatencyMs += latency

            if (eventTimestamps.size > 500) {
                eventTimestamps.removeAt(0)
            }
            if (latenciesMs.size > 500) {
                latenciesMs.removeAt(0)
            }
        }

        fun calculateMetrics(now: Long): PluginBenchmarkMetrics {
            if (eventCount == 0L) {
                return PluginBenchmarkMetrics(pluginId = pluginId)
            }

            val recentWindowMs = 3000L
            val cutoff = now - recentWindowMs
            val recentEventsCount = eventTimestamps.count { it >= cutoff }
            val eps = (recentEventsCount.toDouble() / (recentWindowMs / 1000.0))

            val avgLatency = if (latenciesMs.isNotEmpty()) latenciesMs.average() else 0.0
            val minLat = if (minLatencyMs == Double.MAX_VALUE) 0.0 else minLatencyMs

            return PluginBenchmarkMetrics(
                pluginId = pluginId,
                eventCount = eventCount,
                eventsPerSecond = (eps * 10.0).roundToInt() / 10.0,
                averageLatencyMs = (avgLatency * 10.0).roundToInt() / 10.0,
                minLatencyMs = (minLat * 10.0).roundToInt() / 10.0,
                maxLatencyMs = (maxLatencyMs * 10.0).roundToInt() / 10.0,
                lastEventTimestamp = lastEventTimestamp
            )
        }
    }

    private val trackers = mutableMapOf<PluginId, PluginTracker>()
    private val lock = Any()

    /**
     * Records an event emission for a specific plugin.
     */
    fun recordEvent(
        pluginId: PluginId,
        eventTimestamp: Long = currentTimestamp(),
        receiveTimestamp: Long = currentTimestamp()
    ) {
        synchronized(lock) {
            val tracker = trackers.getOrPut(pluginId) { PluginTracker(pluginId) }
            tracker.record(eventTimestamp, receiveTimestamp)
        }
    }

    /**
     * Returns a snapshot report of current live benchmarking statistics.
     */
    fun getSnapshot(): BenchmarkReport {
        val now = currentTimestamp()
        synchronized(lock) {
            if (trackers.isEmpty()) {
                return BenchmarkReport(
                    memoryUsageMb = ((getMemoryUsageMb() * 10.0).roundToInt() / 10.0),
                    timestamp = now
                )
            }

            val metricsMap = trackers.mapValues { (_, tracker) -> tracker.calculateMetrics(now) }
            val activePluginsCount = metricsMap.values.count { (now - it.lastEventTimestamp) < 10000L }
            val totalEvents = metricsMap.values.sumOf { it.eventCount }
            val totalEps = metricsMap.values.sumOf { it.eventsPerSecond }
            val validLatencies = metricsMap.values.filter { it.eventCount > 0 }

            val overallAvgLatency = if (validLatencies.isNotEmpty()) {
                validLatencies.map { it.averageLatencyMs }.average()
            } else 0.0

            val overallMaxLatency = if (validLatencies.isNotEmpty()) {
                validLatencies.maxOf { it.maxLatencyMs }
            } else 0.0

            return BenchmarkReport(
                activePluginsCount = activePluginsCount,
                totalEventsCount = totalEvents,
                totalEventsPerSecond = (totalEps * 10.0).roundToInt() / 10.0,
                averageLatencyMs = (overallAvgLatency * 10.0).roundToInt() / 10.0,
                maxLatencyMs = (overallMaxLatency * 10.0).roundToInt() / 10.0,
                memoryUsageMb = ((getMemoryUsageMb() * 10.0).roundToInt() / 10.0),
                pluginMetrics = metricsMap,
                timestamp = now
            )
        }
    }

    /**
     * Emits continuous benchmark reports at the specified sample interval.
     */
    fun observeReport(sampleIntervalMs: Long = 1000L): Flow<BenchmarkReport> = flow {
        while (true) {
            emit(getSnapshot())
            delay(sampleIntervalMs)
        }
    }

    /**
     * Checks if active benchmarking events have been recorded in the past 10 seconds.
     */
    fun isBenchmarkingActive(): Boolean {
        val now = currentTimestamp()
        synchronized(lock) {
            return trackers.values.any { (now - it.lastEventTimestamp) < 10000L }
        }
    }

    /**
     * Resets all accumulated benchmark metrics.
     */
    fun reset() {
        synchronized(lock) {
            trackers.clear()
        }
    }
}

/**
 * Extension function to automatically measure and benchmark a KSensor data Flow.
 */
fun <T> Flow<KSensorResponse<T>>.benchmark(pluginId: PluginId): Flow<KSensorResponse<T>> {
    return this.onEach { response ->
        BenchmarkMonitor.recordEvent(
            pluginId = pluginId,
            eventTimestamp = response.timestamp,
            receiveTimestamp = currentTimestamp()
        )
    }
}
