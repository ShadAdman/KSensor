package com.ksensor.core.model


enum class DeviceOrientation {
    PORTRAIT, LANDSCAPE, UNKNOWN
}

enum class TouchGestureType {
    ACTION_DOWN, ACTION_UP, ACTION_MOVE, TAP, DOUBLE_TAP, LONG_PRESS
}

enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    BAROMETER,
    STEP_COUNTER,
    MOTION_DETECTOR,
    LOCATION,
    DEVICE_ORIENTATION,
    PROXIMITY,
    LIGHT,
    TOUCH_GESTURES,
    HEADING,
    HEART_RATE,
    FUSED_ACTIVITY,
    FUSED_POSTURE,
    FUSED_3D_ORIENTATION,
    FUSED_SURROUNDING
}

sealed class SensorData {
    data class Accelerometer(val values: Vector3) : SensorData()
    data class Gyroscope(val values: Vector3) : SensorData()
    data class Magnetometer(val values: Vector3) : SensorData()

    data class Barometer(val pressure: Float) : SensorData()
    data class StepCounter(val steps: Int) : SensorData()
    
    enum class MotionType {
        STATIONARY, WALKING, RUNNING, CYCLING, AUTOMOTIVE, UNKNOWN
    }
    data class MotionDetector(val type: MotionType) : SensorData()

    data class Location(
        val latitude: Double? = null,
        val longitude: Double? = null,
        val altitude: Double? = null
    ) : SensorData()

    data class Orientation(
        val orientation: DeviceOrientation,
        val orientationInt: Int = 0
    ) : SensorData()

    data class Proximity(
        val distanceInCM: Float,
        val isNear: Boolean
    ): SensorData()

    data class LightIlluminance(
        val illuminance: Float
    ): SensorData()

    data class TouchGestures(val x: Float,val y: Float,val type: TouchGestureType): SensorData()

    data class Heading(
        val magneticHeading: Double,
        val trueHeading: Double,
        val deviceHeading: Double,
        val courseOverGround: Double
    ) : SensorData()

    data class Noise(
        val dB: Float,
        val isLoud: Boolean? = null,
        val timestamp: Long? = null
    ) : SensorData()

    enum class HeartRateSource {
        HARDWARE_SENSOR,
        CAMERA_PPG,
        UNKNOWN
    }
    data class HeartRate(
        val heartRate: Float, 
        val source: HeartRateSource = HeartRateSource.UNKNOWN,
        val confidence: Float = 1.0f,
        val quality: Float = 1.0f,
        val timestamp: Long = 0L
    ) : SensorData()

    // Fused Sensor Outputs
    enum class FusedActivityType {
        STATIONARY, WALKING, RUNNING, CYCLING, AUTOMOTIVE, UNKNOWN
    }
    data class FusedActivity(
        override val result: FusedActivityType,
        override val confidence: FusedConfidence,
        override val sources: List<FusedSourceInfo>,
        override val requiredSources: List<PluginId> = listOf(
            PluginId.ACCELEROMETER, PluginId.GYROSCOPE, PluginId.STEP_COUNTER, PluginId.MOTION_DETECTOR
        )
    ) : SensorData(), FusedSensorOutput<FusedActivityType>

    enum class FusedPostureType {
        SITTING, STANDING, LYING_DOWN, IN_HAND, IN_POCKET, AT_EAR, UNKNOWN
    }
    data class FusedPosture(
        override val result: FusedPostureType,
        override val confidence: FusedConfidence,
        override val sources: List<FusedSourceInfo>,
        override val requiredSources: List<PluginId> = listOf(
            PluginId.ACCELEROMETER, PluginId.GYROSCOPE, PluginId.PROXIMITY, PluginId.ORIENTATION
        )
    ) : SensorData(), FusedSensorOutput<FusedPostureType>

    data class Fused3DOrientation(
        val orientation: DeviceOrientation,
        val pitchDegrees: Float,
        val rollDegrees: Float,
        val yawDegrees: Float,
        val isStable: Boolean,
        override val confidence: FusedConfidence,
        override val sources: List<FusedSourceInfo>,
        override val requiredSources: List<PluginId> = listOf(
            PluginId.ACCELEROMETER, PluginId.GYROSCOPE, PluginId.MAGNETOMETER, PluginId.ORIENTATION
        )
    ) : SensorData(), FusedSensorOutput<DeviceOrientation> {
        override val result: DeviceOrientation get() = orientation
    }

    enum class FusedEnvironmentState { INDOOR, OUTDOOR, IN_VEHICLE, ELEVATED, UNKNOWN }
    enum class FusedMotionState { STILL, IN_MOTION, HIGH_SPEED, UNKNOWN }

    data class FusedSurrounding(
        val environmentState: FusedEnvironmentState,
        val motionState: FusedMotionState,
        val isIndoorEstimate: Boolean,
        val noiseLevelSummary: String,
        override val confidence: FusedConfidence,
        override val sources: List<FusedSourceInfo>,
        override val requiredSources: List<PluginId> = listOf(
            PluginId.MOTION_DETECTOR, PluginId.LOCATION, PluginId.LIGHT, PluginId.BAROMETER, PluginId.NOISE
        )
    ) : SensorData(), FusedSensorOutput<FusedEnvironmentState> {
        override val result: FusedEnvironmentState get() = environmentState
    }
}
