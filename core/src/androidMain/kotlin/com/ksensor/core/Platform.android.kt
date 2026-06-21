package com.ksensor.core

actual fun currentPlatform(): PlatformType = PlatformType.Android
actual fun currentTimestamp(): Long = System.currentTimeMillis()
