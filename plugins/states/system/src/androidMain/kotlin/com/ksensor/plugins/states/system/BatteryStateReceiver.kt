package com.ksensor.plugins.states.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.ksensor.core.model.StateData

internal class BatteryStateReceiver(private val onData: (StateData.BatteryStatus) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        onData(parseIntent(intent))
    }

    companion object {
        fun parseIntent(intent: Intent): StateData.BatteryStatus {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent: Int? = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else null

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val chargingState = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> StateData.BatteryStatus.ChargingState.CHARGING
                BatteryManager.BATTERY_STATUS_FULL -> StateData.BatteryStatus.ChargingState.FULL
                BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> StateData.BatteryStatus.ChargingState.DISCHARGING
                else -> StateData.BatteryStatus.ChargingState.UNKNOWN
            }

            val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            val health = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> StateData.BatteryStatus.BatteryHealth.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> StateData.BatteryStatus.BatteryHealth.OVERHEAT
                BatteryManager.BATTERY_HEALTH_DEAD -> StateData.BatteryStatus.BatteryHealth.DEAD
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> StateData.BatteryStatus.BatteryHealth.OVER_VOLTAGE
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> StateData.BatteryStatus.BatteryHealth.UNSPECIFIED_FAILURE
                BatteryManager.BATTERY_HEALTH_COLD -> StateData.BatteryStatus.BatteryHealth.COLD
                else -> StateData.BatteryStatus.BatteryHealth.UNKNOWN
            }

            val tempDeciC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            val temperatureC = if (tempDeciC != Int.MIN_VALUE) tempDeciC / 10f else null

            return StateData.BatteryStatus(
                levelPercent = percent,
                chargingState = chargingState,
                health = health,
                temperatureC = temperatureC,
            )
        }

        fun getCurrentStatus(context: Context): StateData.BatteryStatus {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            return if (intent != null) {
                parseIntent(intent)
            } else {
                StateData.BatteryStatus(null, StateData.BatteryStatus.ChargingState.UNKNOWN, null, null)
            }
        }
    }
}
