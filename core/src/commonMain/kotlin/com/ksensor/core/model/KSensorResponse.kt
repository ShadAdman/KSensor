package com.ksensor.core.model

import com.ksensor.core.PlatformType
import com.ksensor.core.currentTimestamp

/**
 * A wrapper for sensor and state data that includes platform information and a timestamp.
 */
data class KSensorResponse<T>(
    val data: T,
    val platform: PlatformType,
    val timestamp: Long = currentTimestamp()
)
