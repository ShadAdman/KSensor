package com.ksensor.core.benchmark

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock as javaWithLock

internal actual class KSensorLock actual constructor() {
    private val lock = ReentrantLock()
    actual fun <T> withLock(block: () -> T): T = lock.javaWithLock(block)
}
