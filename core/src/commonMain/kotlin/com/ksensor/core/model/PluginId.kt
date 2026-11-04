package com.ksensor.core.model

/**
 * Unique identifiers for KSensor plugins.
 */
enum class PluginId {
    MOTION,
    ENVIRONMENT,
    NOISE,
    POSITIONING,
    INTERACTION,
    NETWORK,
    SYSTEM,
    BLUETOOTH,
    LIFECYCLE,
    HEALTH,

    // Atomic Sensors
    ACCELEROMETER,
    GYROSCOPE,
    STEP_COUNTER,
    MOTION_DETECTOR,
    BAROMETER,
    LIGHT,
    PROXIMITY,
    LOCATION,
    MAGNETOMETER,
    ORIENTATION,
    HEADING,
    LOCATION_STATUS,
    TOUCH_GESTURES,
    SCROLL_STATUS,
    HEART_RATE,

    // Atomic States
    BLE_CONNECTIONS,
    BLE_DISCOVERIES,
    BLE_AUDIO_DEVICES,
    APP_VISIBILITY,
    CONNECTIVITY,
    ACTIVE_NETWORK,
    CALL_STATE,
    BATTERY,
    VOLUME,
    LOCALE,
    SCREEN,
    BRIGHTNESS,
    LOCK,
    POWER_SAVE,
    STORAGE,
    RESOURCES,
}