package com.ksensor.plugins.states.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
            get() = StateData.BatteryStatus(null, StateData.BatteryStatus.ChargingState.UNKNOWN, null, null)

        override fun observe(): Flow<StateData.BatteryStatus> = callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else null

                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val chargingState = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> StateData.BatteryStatus.ChargingState.CHARGING
                        BatteryManager.BATTERY_STATUS_FULL -> StateData.BatteryStatus.ChargingState.FULL
                        else -> StateData.BatteryStatus.ChargingState.DISCHARGING
                    }
                    trySend(StateData.BatteryStatus(percent, chargingState, null, null))
                }
            }
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            awaitClose { context.unregisterReceiver(receiver) }
        }
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = TODO()
    override fun locale(): StatePlugin<StateData.LocaleStatus> = TODO()
    override fun screen(): StatePlugin<StateData.ScreenStatus> = TODO()
    override fun lock(): StatePlugin<StateData.LockStatus> = TODO()
}

actual fun createSystemPlugin(): SystemPlugin = AndroidSystemPlugin()
