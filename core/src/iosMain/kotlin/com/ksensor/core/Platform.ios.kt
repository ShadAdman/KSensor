package com.ksensor.core

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentPlatform(): PlatformType = PlatformType.iOS
actual fun currentTimestamp(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
