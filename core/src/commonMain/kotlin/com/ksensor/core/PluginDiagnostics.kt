package com.ksensor.core

import com.ksensor.core.model.PluginId

/**
 * Lifecycle and health diagnostics information for a KSensor plugin.
 */
interface PluginDiagnostics {
    val pluginId: PluginId
    val status: PluginStatus
    val lastUpdatedTimestamp: Long
    val lastErrorMessage: String?
    val unsupportedReason: String?
    val permissionState: PermissionStatus
    val isPlatformAvailable: Boolean

    val isHealthy: Boolean
        get() = status == PluginStatus.ACTIVE || status == PluginStatus.READY || status == PluginStatus.IDLE
}

/**
 * Default concrete implementation of [PluginDiagnostics].
 */
data class DefaultPluginDiagnostics(
    override val pluginId: PluginId,
    override val status: PluginStatus = PluginStatus.IDLE,
    override val lastUpdatedTimestamp: Long = currentTimestamp(),
    override val lastErrorMessage: String? = null,
    override val unsupportedReason: String? = null,
    override val permissionState: PermissionStatus = PermissionStatus.UNKNOWN,
    override val isPlatformAvailable: Boolean = true
) : PluginDiagnostics
