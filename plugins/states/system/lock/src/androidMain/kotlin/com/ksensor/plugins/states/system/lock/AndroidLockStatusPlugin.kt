package com.ksensor.plugins.states.system.lock

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class AndroidLockStatusPlugin : LockStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val keyguardManager by lazy { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.LockStatus>
        get() = KSensorResponse(StateData.LockStatus(keyguardManager.isDeviceLocked))

    private val lockFlow by lazy {
        callbackFlow {
            val receiver = ScreenStateReceiver(
                onScreenOff = { trySend(KSensorResponse(StateData.LockStatus(keyguardManager.isDeviceSecure))) },
                onUserPresent = { trySend(KSensorResponse(StateData.LockStatus(false))) }
            )
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            context.registerReceiver(receiver, filter)
            awaitClose { context.unregisterReceiver(receiver) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.LockStatus>> = lockFlow
}

actual fun createLockStatusPlugin(): LockStatusPlugin = AndroidLockStatusPlugin()
