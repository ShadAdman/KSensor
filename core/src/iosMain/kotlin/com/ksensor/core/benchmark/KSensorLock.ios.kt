package com.ksensor.core.benchmark

import platform.Foundation.NSLock

internal actual class KSensorLock actual constructor() {
    private val lock = NSLock()
    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
