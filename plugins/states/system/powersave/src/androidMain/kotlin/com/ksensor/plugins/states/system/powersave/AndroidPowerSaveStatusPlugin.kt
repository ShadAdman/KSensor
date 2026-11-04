package com.ksensor.plugins.states.system.powersave

import android.content.Context
import android.content.IntentFilter
import android.os.PowerManager
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

class AndroidPowerSaveStatusPlugin : PowerSaveStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.PowerSaveStatus>
        get() = KSensorResponse(PowerSaveReceiver.getCurrentStatus(context))

    private val powerSaveFlow by lazy {
        callbackFlow {
            val receiver = PowerSaveReceiver { trySend(KSensorResponse(it)) }
            context.registerReceiver(receiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
            trySend(KSensorResponse(PowerSaveReceiver.getCurrentStatus(context)))
            awaitClose { context.unregisterReceiver(receiver) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.PowerSaveStatus>> = powerSaveFlow
}

actual fun createPowerSaveStatusPlugin(): PowerSaveStatusPlugin = AndroidPowerSaveStatusPlugin()
