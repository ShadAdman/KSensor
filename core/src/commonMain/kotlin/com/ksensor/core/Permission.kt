package com.ksensor.core

/**
 * Abstraction for permissions required by sensors and state plugins.
 */
enum class Permission {
    LOCATION,
    BLUETOOTH,
    ACTIVITY_RECOGNITION,
    BODY_SENSORS
}

/**
 * Interface for handling permission requests across platforms.
 */
interface PermissionHandler {
    fun hasPermission(permission: Permission): Boolean
    suspend fun requestPermission(permission: Permission): Boolean
}
