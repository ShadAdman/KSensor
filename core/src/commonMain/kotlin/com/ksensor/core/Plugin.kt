package com.ksensor.core

import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all KSensor plugins.
 */
interface KSensorPlugin {
    val id: String
    val requiredPermissions: List<Permission>
}

/**
 * Interface for sensors that provide a stream of data.
 */
interface SensorPlugin<T> : KSensorPlugin {
    fun observe(config: SensorConfig = SensorConfig.Default): Flow<T>
}

/**
 * Interface for state providers that have a current value and a stream of changes.
 */
interface StatePlugin<T> : KSensorPlugin {
    val currentState: T
    fun observe(): Flow<T>
}
