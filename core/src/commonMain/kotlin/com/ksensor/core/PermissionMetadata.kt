package com.ksensor.core

/**
 * Target platform for permission requirements.
 */
enum class TargetPlatform {
    COMMON,
    ANDROID,
    IOS
}

/**
 * Metadata describing a platform-specific permission requirement.
 *
 * @property platform Target platform (ANDROID, IOS, or COMMON).
 * @property permissionName Platform permission identifier (e.g., "android.permission.ACCESS_FINE_LOCATION" or "NSLocationWhenInUseUsageDescription").
 * @property usageDescriptionKey Key used for iOS Info.plist or localization.
 * @property usageDescriptionDefault Default human-readable usage description (for iOS Info.plist or rationale dialogs).
 * @property isRequired Indicates whether this permission is mandatory for basic plugin functionality.
 * @property minSdkVersion Minimum Android SDK version for this permission requirement.
 * @property maxSdkVersion Maximum Android SDK version for this permission requirement.
 * @property extraAttributes Additional XML/Plist attributes or flags.
 */
data class PermissionMetadata(
    val platform: TargetPlatform = TargetPlatform.COMMON,
    val permissionName: String,
    val usageDescriptionKey: String? = null,
    val usageDescriptionDefault: String? = null,
    val isRequired: Boolean = true,
    val minSdkVersion: Int? = null,
    val maxSdkVersion: Int? = null,
    val extraAttributes: Map<String, String> = emptyMap()
)

/**
 * Maps standard high-level [Permission] enum values to platform-specific [PermissionMetadata] entries.
 */
fun Permission.toMetadataList(): List<PermissionMetadata> = when (this) {
    Permission.LOCATION -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.ACCESS_FINE_LOCATION",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.ACCESS_COARSE_LOCATION",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSLocationWhenInUseUsageDescription",
            usageDescriptionKey = "NSLocationWhenInUseUsageDescription",
            usageDescriptionDefault = "This app requires access to location services to track positioning data.",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSLocationAlwaysAndWhenInUseUsageDescription",
            usageDescriptionKey = "NSLocationAlwaysAndWhenInUseUsageDescription",
            usageDescriptionDefault = "This app requires background location access to track positioning data.",
            isRequired = false
        )
    )

    Permission.BLUETOOTH -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.BLUETOOTH_SCAN",
            minSdkVersion = 31,
            extraAttributes = mapOf("usesPermissionFlags" to "neverForLocation"),
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.BLUETOOTH_CONNECT",
            minSdkVersion = 31,
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.BLUETOOTH",
            maxSdkVersion = 30,
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.BLUETOOTH_ADMIN",
            maxSdkVersion = 30,
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSBluetoothAlwaysUsageDescription",
            usageDescriptionKey = "NSBluetoothAlwaysUsageDescription",
            usageDescriptionDefault = "This app requires Bluetooth access to observe Bluetooth state and connected devices.",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSBluetoothPeripheralUsageDescription",
            usageDescriptionKey = "NSBluetoothPeripheralUsageDescription",
            usageDescriptionDefault = "This app requires Bluetooth peripheral access.",
            isRequired = false
        )
    )

    Permission.ACTIVITY_RECOGNITION -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.ACTIVITY_RECOGNITION",
            minSdkVersion = 29,
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSMotionUsageDescription",
            usageDescriptionKey = "NSMotionUsageDescription",
            usageDescriptionDefault = "This app requires Motion & Fitness access to detect physical motion and activity.",
            isRequired = true
        )
    )

    Permission.BODY_SENSORS -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.BODY_SENSORS",
            minSdkVersion = 20,
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.health.READ_HEART_RATE",
            minSdkVersion = 34,
            isRequired = false
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSHealthShareUsageDescription",
            usageDescriptionKey = "NSHealthShareUsageDescription",
            usageDescriptionDefault = "This app requires Health access to read health sensor metrics.",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSHealthUpdateUsageDescription",
            usageDescriptionKey = "NSHealthUpdateUsageDescription",
            usageDescriptionDefault = "This app requires Health access to write health sensor metrics.",
            isRequired = false
        )
    )

    Permission.CAMERA -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.CAMERA",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSCameraUsageDescription",
            usageDescriptionKey = "NSCameraUsageDescription",
            usageDescriptionDefault = "This app requires Camera access.",
            isRequired = true
        )
    )

    Permission.RECORD_AUDIO -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.RECORD_AUDIO",
            isRequired = true
        ),
        PermissionMetadata(
            platform = TargetPlatform.IOS,
            permissionName = "NSMicrophoneUsageDescription",
            usageDescriptionKey = "NSMicrophoneUsageDescription",
            usageDescriptionDefault = "This app requires Microphone access to measure ambient sound levels.",
            isRequired = true
        )
    )

    Permission.READ_PHONE_STATE -> listOf(
        PermissionMetadata(
            platform = TargetPlatform.ANDROID,
            permissionName = "android.permission.READ_PHONE_STATE",
            isRequired = true
        )
    )
}
