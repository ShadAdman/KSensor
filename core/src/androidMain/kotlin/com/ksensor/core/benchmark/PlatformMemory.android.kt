package com.ksensor.core.benchmark

actual fun getMemoryUsageMb(): Double {
    val runtime = Runtime.getRuntime()
    val usedBytes = runtime.totalMemory() - runtime.freeMemory()
    return usedBytes.toDouble() / (1024.0 * 1024.0)
}
