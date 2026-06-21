[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-green.svg?style=flat-square&logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-0BSD-informational.svg)](https://opensource.org/licenses/0BSD)

<p align="center">
  <img src="ksensor.png" alt="ksensor Poster" width="600" style="border-radius: 50%;"/>
</p>

# KSensor

KSensor is a plugin-based Kotlin Multiplatform library for observing device sensors and system states. Each sensor or state is grouped into its own plugin, allowing you to include only the features you need. This prevents pulling in unnecessary code and permissions.

## Core Module

The foundation of the library. It is required for all plugins.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-core:2.0.0")
```

## Motion Sensors Plugin

Provides access to high-frequency hardware sensors for tracking movement.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-sensors-motion:2.0.0")
```

Representations and Data Models:

- Accelerometer: `Accelerometer(values: Vector3)`
- Gyroscope: `Gyroscope(values: Vector3)`
- Step Counter: `StepCounter(steps: Int)`
- Step Detector: `StepDetector` (data object)

## Environment Sensors Plugin

Provides data from sensors that monitor the ambient environment.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-sensors-environment:2.0.0")
```

Representations and Data Models:

- Barometer: `Barometer(pressure: Float)`
- Light: `LightIlluminance(illuminance: Float)`
- Proximity: `Proximity(distanceInCM: Float, isNear: Boolean)`

## Positioning Sensors Plugin

Provides location services and spatial orientation data.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-sensors-positioning:2.0.0")
```

Representations and Data Models:

- Location: `Location(latitude: Double?, longitude: Double?, altitude: Double?)`
- Magnetometer: `Magnetometer(values: Vector3)`
- Orientation: `Orientation(orientation: DeviceOrientation, orientationInt: Int)`
- Location Status: `LocationStatus(isLocationOn: Boolean)`

## Interaction Sensors Plugin

Provides high-level data related to user input gestures.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-sensors-interaction:2.0.0")
```

Representations and Data Models:

- Touch Gestures: `TouchGestures(x: Float, y: Float, type: TouchGestureType)`

## Network States Plugin

Provides information about the network connectivity of the device.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-states-network:2.0.0")
```

Representations and Data Models:

- Connectivity: `ConnectivityStatus(isConnected: Boolean)`
- Active Network: `CurrentActiveNetwork(activeNetwork: ActiveNetwork)` (Values: WIFI, CELLULAR, NONE)

## System States Plugin

Provides access to general device system states like battery and volume.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-states-system:2.0.0")
```

Representations and Data Models:

- Battery: `BatteryStatus(levelPercent: Int?, chargingState: ChargingState, health: BatteryHealth?, temperatureC: Float?)`
- Volume: `VolumeStatus(volumePercentage: Int)`
- Locale: `LocaleStatus(languageCode: String, countryCode: String, fullLocaleString: String, displayName: String, isRTL: Boolean)`
- Screen: `ScreenStatus(isScreenOn: Boolean)`
- Lock: `LockStatus(isDeviceLocked: Boolean)`

## Bluetooth States Plugin

Provides monitoring for BLE connection and discovery events.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-states-bluetooth:2.0.0")
```

Representations and Data Models:

- BLE Connections: `BleConnectionStatus(connectedDevices: List<BleDevice>)`
- BLE Discoveries: `BleDiscoversStatus(discoveredDevices: List<BleDevice>)`
- BLE Device: `BleDevice(id: String, name: String)`

## Lifecycle States Plugin

Tracks the visibility and lifecycle state of the application.

Dependency:
```kotlin
implementation("io.github.shadadman:ksensor-states-lifecycle:2.0.0")
```

Representations and Data Models:

- App Visibility: `AppVisibilityStatus(isAppVisible: Boolean)`

## Basic Usage

1. Register your plugin implementation (usually via a factory method like `createMotionPlugin()`).
2. Use the `KSensor` registry to retrieve the plugin and observe its data using Kotlin Flow.

Example:
```kotlin
val motionPlugin = createMotionPlugin()
KSensor.register(motionPlugin)

val motion = KSensor.get<MotionPlugin>("ksensor.sensors.motion")
motion?.accelerometer()?.collect { data ->
    // Access data.values.x, data.values.y, data.values.z
}
```

## License

Copyright (c) 2025 KSensor

Permission to use, copy, modify, and/or distribute this software for any purpose
with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
