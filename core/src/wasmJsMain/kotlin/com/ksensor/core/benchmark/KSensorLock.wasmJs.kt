package com.ksensor.core.benchmark

internal actual class KSensorLock actual constructor() {
    actual fun <T> withLock(block: () -> T): T = block()
}
