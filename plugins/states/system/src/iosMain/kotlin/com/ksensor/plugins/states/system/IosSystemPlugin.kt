package com.ksensor.plugins.states.system

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationProtectedDataDidBecomeAvailable
import platform.UIKit.UIApplicationProtectedDataWillBecomeUnavailable
import platform.darwin.NSObject

class IosSystemPlugin : SystemPlugin {
    override val id: String = "ksensor.states.system"
    override val requiredPermissions: List<Permission> = emptyList()

    override fun battery(): StatePlugin<StateData.BatteryStatus> = object : StatePlugin<StateData.BatteryStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.battery"
        override val requiredPermissions: List<Permission> = emptyList()
        private val receiver = BatteryStateReceiver {}
        override val currentState: StateData.BatteryStatus get() = receiver.getCurrentStatus()

        override fun observe(): Flow<StateData.BatteryStatus> = callbackFlow {
            val obs = BatteryStateReceiver { trySend(it) }
            obs.register()
            awaitClose { obs.unregister() }
        }
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = object : StatePlugin<StateData.VolumeStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.volume"
        override val requiredPermissions: List<Permission> = emptyList()
        private val receiver = VolumeReceiver {}
        override val currentState: StateData.VolumeStatus get() = StateData.VolumeStatus(receiver.getCurrentVolume())

        override fun observe(): Flow<StateData.VolumeStatus> = callbackFlow {
            val obs = VolumeReceiver { trySend(StateData.VolumeStatus(it)) }
            obs.register()
            awaitClose { obs.unregister() }
        }
    }

    override fun locale(): StatePlugin<StateData.LocaleStatus> = object : StatePlugin<StateData.LocaleStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.locale"
        override val requiredPermissions: List<Permission> = emptyList()
        private val receiver = LocaleReceiver {}
        override val currentState: StateData.LocaleStatus get() = receiver.getCurrentLocale()

        override fun observe(): Flow<StateData.LocaleStatus> = callbackFlow {
            val obs = LocaleReceiver { trySend(it) }
            obs.register()
            awaitClose { obs.unregister() }
        }
    }

    override fun screen(): StatePlugin<StateData.ScreenStatus> = object : StatePlugin<StateData.ScreenStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.screen"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.ScreenStatus get() = StateData.ScreenStatus(true)

        override fun observe(): Flow<StateData.ScreenStatus> = callbackFlow {
            val obs = ScreenStateReceiver { trySend(StateData.ScreenStatus(it)) }
            obs.register()
            awaitClose { obs.unregister() }
        }
    }

    override fun lock(): StatePlugin<StateData.LockStatus> = object : StatePlugin<StateData.LockStatus> {
        override val id: String = "${this@IosSystemPlugin.id}.lock"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.LockStatus 
            get() = StateData.LockStatus(!UIApplication.sharedApplication.isProtectedDataAvailable())

        override fun observe(): Flow<StateData.LockStatus> = callbackFlow {
            val lockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIApplicationProtectedDataWillBecomeUnavailable,
                `object` = null,
                queue = NSOperationQueue.mainQueue
            ) { trySend(StateData.LockStatus(true)) }

            val unlockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIApplicationProtectedDataDidBecomeAvailable,
                `object` = null,
                queue = NSOperationQueue.mainQueue
            ) { trySend(StateData.LockStatus(false)) }

            awaitClose {
                NSNotificationCenter.defaultCenter.removeObserver(lockObserver)
                NSNotificationCenter.defaultCenter.removeObserver(unlockObserver)
            }
        }
    }
}

actual fun createSystemPlugin(): SystemPlugin = IosSystemPlugin()
