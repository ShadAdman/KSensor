package com.ksensor.plugins.states.system

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidSystemPlugin : SystemPlugin {
    override val id: String = "ksensor.states.system"
    override val requiredPermissions: List<Permission> = emptyList()

    private val context: Context by lazy { KSensorContext.get() }

    override fun battery(): StatePlugin<StateData.BatteryStatus> = object : StatePlugin<StateData.BatteryStatus> {
        override val id: String = "${this@AndroidSystemPlugin.id}.battery"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.BatteryStatus
            get() = BatteryStateReceiver.getCurrentStatus(context)

        override fun observe(): Flow<StateData.BatteryStatus> = callbackFlow {
            val receiver = BatteryStateReceiver { trySend(it) }
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            awaitClose { context.unregisterReceiver(receiver) }
        }
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = object : StatePlugin<StateData.VolumeStatus> {
        override val id: String = "${this@AndroidSystemPlugin.id}.volume"
        override val requiredPermissions: List<Permission> = emptyList()
        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        override val currentState: StateData.VolumeStatus
            get() = StateData.VolumeStatus(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))

        override fun observe(): Flow<StateData.VolumeStatus> = callbackFlow {
            val receiver = VolumeReceiver(audioManager) { trySend(StateData.VolumeStatus(it)) }
            context.registerReceiver(receiver, IntentFilter(VOLUME_CHANGED_ACTION))
            awaitClose { context.unregisterReceiver(receiver) }
        }
    }

    override fun locale(): StatePlugin<StateData.LocaleStatus> = object : StatePlugin<StateData.LocaleStatus> {
        override val id: String = "${this@AndroidSystemPlugin.id}.locale"
        override val requiredPermissions: List<Permission> = emptyList()
        private val receiver = LocaleReceiver(context) {}
        override val currentState: StateData.LocaleStatus
            get() = receiver.getCurrentLocale()

        override fun observe(): Flow<StateData.LocaleStatus> = callbackFlow {
            val obs = LocaleReceiver(context) { trySend(it) }
            context.registerReceiver(obs, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
            awaitClose { context.unregisterReceiver(obs) }
        }
    }

    override fun screen(): StatePlugin<StateData.ScreenStatus> = object : StatePlugin<StateData.ScreenStatus> {
        override val id: String = "${this@AndroidSystemPlugin.id}.screen"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.ScreenStatus
            get() = StateData.ScreenStatus(true) // Approximate

        override fun observe(): Flow<StateData.ScreenStatus> = callbackFlow {
            val receiver = ScreenStateReceiver(
                onScreenOn = { trySend(StateData.ScreenStatus(true)) },
                onScreenOff = { trySend(StateData.ScreenStatus(false)) }
            )
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(receiver, filter)
            awaitClose { context.unregisterReceiver(receiver) }
        }
    }

    override fun lock(): StatePlugin<StateData.LockStatus> = object : StatePlugin<StateData.LockStatus> {
        override val id: String = "${this@AndroidSystemPlugin.id}.lock"
        override val requiredPermissions: List<Permission> = emptyList()
        private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        override val currentState: StateData.LockStatus
            get() = StateData.LockStatus(keyguardManager.isDeviceLocked)

        override fun observe(): Flow<StateData.LockStatus> = callbackFlow {
            val receiver = ScreenStateReceiver(
                onScreenOff = { trySend(StateData.LockStatus(keyguardManager.isDeviceSecure)) },
                onUserPresent = { trySend(StateData.LockStatus(false)) }
            )
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            context.registerReceiver(receiver, filter)
            awaitClose { context.unregisterReceiver(receiver) }
        }
    }
}

actual fun createSystemPlugin(): SystemPlugin = AndroidSystemPlugin()
