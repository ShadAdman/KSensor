package com.ksensor.plugins.states.system.screen

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

class AndroidScreenStatusPlugin : ScreenStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.ScreenStatus>
        get() = KSensorResponse(StateData.ScreenStatus(true))

    private val screenFlow by lazy {
        callbackFlow {
            val receiver = ScreenStateReceiver(
                onScreenOn = { trySend(KSensorResponse(StateData.ScreenStatus(true))) },
                onScreenOff = { trySend(KSensorResponse(StateData.ScreenStatus(false))) }
            )
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(receiver, filter)
            awaitClose { context.unregisterReceiver(receiver) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.ScreenStatus>> = screenFlow
}

actual fun createScreenStatusPlugin(): ScreenStatusPlugin = AndroidScreenStatusPlugin()
