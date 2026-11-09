package com.ksensor.core.benchmark

import platform.Foundation.NSProcessInfo

actual fun getMemoryUsageMb(): Double {
    val bytes = NSProcessInfo.processInfo.physicalMemory.toDouble()
    return (bytes / (1024.0 * 1024.0)).coerceAtMost(512.0)
}
