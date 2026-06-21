package com.ksensor.plugins.states.lifecycle

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

class IosLifecyclePlugin : LifecyclePlugin {
    override val id: String = "ksensor.states.lifecycle"
    override val requiredPermissions: List<Permission> = emptyList()

    override fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus> = object : StatePlugin<StateData.AppVisibilityStatus> {
        override val id: String = "${this@IosLifecyclePlugin.id}.visibility"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.AppVisibilityStatus = StateData.AppVisibilityStatus(true)

        override fun observe(): Flow<StateData.AppVisibilityStatus> = callbackFlow {
            val foregroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIApplicationWillEnterForegroundNotification,
                `object` = null,
                queue = NSOperationQueue.mainQueue
            ) { trySend(StateData.AppVisibilityStatus(true)) }

            val backgroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIApplicationDidEnterBackgroundNotification,
                `object` = null,
                queue = NSOperationQueue.mainQueue
            ) { trySend(StateData.AppVisibilityStatus(false)) }

            awaitClose {
                NSNotificationCenter.defaultCenter.removeObserver(foregroundObserver)
                NSNotificationCenter.defaultCenter.removeObserver(backgroundObserver)
            }
        }
    }
}

actual fun createLifecyclePlugin(): LifecyclePlugin = IosLifecyclePlugin()
