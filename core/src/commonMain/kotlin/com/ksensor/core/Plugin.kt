package com.ksensor.core

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all KSensor plugins.
 */
interface KSensorPlugin {
    val id: PluginId
    val requiredPermissions: List<Permission>
    val permissionMetadata: List<PermissionMetadata>
        get() = requiredPermissions.flatMap { it.toMetadataList() }

    /**
     * Indicates whether this plugin is supported on the current platform/hardware.
     */
    val isSupported: Boolean
        get() = true

    /**
     * Human-readable explanation if the plugin is unsupported on the current platform or hardware.
     */
    val unsupportedReason: String?
        get() = if (!isSupported) "Not supported on platform ${currentPlatform()}" else null

    /**
     * Current diagnostics information for this plugin.
     */
    val diagnostics: PluginDiagnostics
        get() = KSensor.diagnostics(id) ?: DefaultPluginDiagnostics(
            pluginId = id,
            status = if (!isSupported) PluginStatus.UNSUPPORTED else PluginStatus.IDLE,
            unsupportedReason = unsupportedReason,
            isPlatformAvailable = isSupported
        )
}

/**
 * Interface for sensors that provide a stream of data.
 */
interface SensorPlugin<T> : KSensorPlugin {
    fun observe(config: SensorConfig = SensorConfig.Default): Flow<KSensorResponse<T>>
}

/**
 * Interface for state providers that have a current value and a stream of changes.
 */
interface StatePlugin<T> : KSensorPlugin {
    val currentState: KSensorResponse<T>
    fun observe(): Flow<KSensorResponse<T>>
}
