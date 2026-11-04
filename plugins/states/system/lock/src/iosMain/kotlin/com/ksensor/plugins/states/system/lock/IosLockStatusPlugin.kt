package com.ksensor.plugins.states.system.lock

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationProtectedDataDidBecomeAvailable
import platform.UIKit.UIApplicationProtectedDataWillBecomeUnavailable

class IosLockStatusPlugin : LockStatusPlugin {
    override val currentState: KSensorResponse<StateData.LockStatus>
        get() = KSensorResponse(StateData.LockStatus(!UIApplication.sharedApplication.isProtectedDataAvailable()))

    override fun observe(): Flow<KSensorResponse<StateData.LockStatus>> = callbackFlow {
        val lockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationProtectedDataWillBecomeUnavailable,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { trySend(KSensorResponse(StateData.LockStatus(true))) }

        val unlockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationProtectedDataDidBecomeAvailable,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { trySend(KSensorResponse(StateData.LockStatus(false))) }

        awaitClose {
            NSNotificationCenter.defaultCenter.removeObserver(lockObserver)
            NSNotificationCenter.defaultCenter.removeObserver(unlockObserver)
        }
    }
}

actual fun createLockStatusPlugin(): LockStatusPlugin = IosLockStatusPlugin()
