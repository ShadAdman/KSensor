package com.ksensor.core

/**
 * Represents the lifecycle and health status of a KSensor plugin.
 */
enum class PluginStatus {
    /**
     * Plugin is registered, supported, and permissions are granted. Ready to observe.
     */
    READY,

    /**
     * Plugin is actively observing or emitting sensor data / states.
     */
    ACTIVE,

    /**
     * Plugin is registered but currently idle / not actively observing.
     */
    IDLE,

    /**
     * Plugin is unsupported on the current platform or device hardware.
     */
    UNSUPPORTED,

    /**
     * Required runtime permissions are missing or denied.
     */
    BLOCKED_BY_PERMISSION,

    /**
     * Plugin encountered a runtime error or exception during execution or setup.
     */
    ERROR
}
