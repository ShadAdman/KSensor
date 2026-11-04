package com.ksensor.plugins.states.system.battery

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

class AndroidBatteryStatusPlugin : BatteryStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.BatteryStatus>
        get() = KSensorResponse(BatteryStateReceiver.getCurrentStatus(context))

    private val batteryFlow by lazy {
        callbackFlow {
            val receiver = BatteryStateReceiver { trySend(KSensorResponse(it)) }
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            awaitClose { context.unregisterReceiver(receiver) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.BatteryStatus>> = batteryFlow
}

actual fun createBatteryStatusPlugin(): BatteryStatusPlugin = AndroidBatteryStatusPlugin()
