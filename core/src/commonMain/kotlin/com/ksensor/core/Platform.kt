package com.ksensor.core

enum class PlatformType {
    iOS,
    Android
}

expect fun currentPlatform(): PlatformType
expect fun currentTimestamp(): Long
