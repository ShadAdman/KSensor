package com.ksensor.core.model

/**
 * Categorical confidence level for fused outputs.
 */
enum class ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN;

    companion object {
        fun fromScore(score: Float): ConfidenceLevel = when {
            score >= 0.8f -> HIGH
            score >= 0.5f -> MEDIUM
            score >= 0.2f -> LOW
            else -> UNKNOWN
        }
    }
}

/**
 * Reusable confidence model for fused outputs.
 * @property score Quantitative confidence value from 0.0 (0%) to 1.0 (100%).
 * @property level Categorical confidence level (LOW, MEDIUM, HIGH, UNKNOWN).
 * @property quality Metric indicating data freshness/accuracy quality from 0.0 to 1.0.
 * @property explanation Optional human-readable explanation of confidence calculation.
 */
data class FusedConfidence(
    val score: Float,
    val level: ConfidenceLevel = ConfidenceLevel.fromScore(score),
    val quality: Float = 1.0f,
    val explanation: String? = null
)

/**
 * Metadata representation of a sensor/state source contributing to a fused output.
 * @property pluginId Unique identifier of the contributing plugin.
 * @property isAvailable Whether the plugin source is active and providing data.
 * @property weight Contribution weight factor of this source to the fused result (0.0 to 1.0).
 * @property lastUpdated Timestamp of the last sample received from this source.
 */
data class FusedSourceInfo(
    val pluginId: PluginId,
    val isAvailable: Boolean = true,
    val weight: Float = 1.0f,
    val lastUpdated: Long = 0L
)

/**
 * Base contract for composite / fused sensor outputs.
 */
interface FusedSensorOutput<T> {
    val result: T
    val confidence: FusedConfidence
    val sources: List<FusedSourceInfo>
    val requiredSources: List<PluginId>
}
