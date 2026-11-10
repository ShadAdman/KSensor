package com.ksensor.core.benchmark

internal expect class KSensorLock() {
    fun <T> withLock(block: () -> T): T
}
