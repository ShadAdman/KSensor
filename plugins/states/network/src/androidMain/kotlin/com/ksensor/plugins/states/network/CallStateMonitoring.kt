package com.ksensor.plugins.states.network

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.ksensor.core.model.StateData

internal class CallStateMonitoring(
    private val context: Context,
    private val onCallStateChanged: (StateData.CallStateStatus) -> Unit,
) {
    private val telephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private var telephonyCallback: TelephonyCallback? = null
    private var phoneStateListener: PhoneStateListener? = null

    fun startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    onCallStateChanged(getCallStatusForState(state))
                }
            }
            telephonyCallback = callback
            try {
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            } catch (_: SecurityException) {
                onCallStateChanged(StateData.CallStateStatus(StateData.CallStateStatus.CallState.UNKNOWN, isInCall = false))
            }
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    onCallStateChanged(getCallStatusForState(state))
                }
            }
            phoneStateListener = listener
            try {
                @Suppress("DEPRECATION")
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (_: SecurityException) {
                onCallStateChanged(StateData.CallStateStatus(StateData.CallStateStatus.CallState.UNKNOWN, isInCall = false))
            }
        }
    }

    fun stopMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                try {
                    telephonyManager.unregisterTelephonyCallback(it)
                } catch (_: Exception) {}
            }
            telephonyCallback = null
        } else {
            phoneStateListener?.let {
                try {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                } catch (_: Exception) {}
            }
            phoneStateListener = null
        }
    }

    fun getCurrentStatus(): StateData.CallStateStatus {
        val rawState = try {
            @Suppress("DEPRECATION")
            telephonyManager.callState
        } catch (_: SecurityException) {
            TelephonyManager.CALL_STATE_IDLE
        }
        return getCallStatusForState(rawState)
    }

    private fun getCallStatusForState(rawState: Int): StateData.CallStateStatus {
        val mappedState = when (rawState) {
            TelephonyManager.CALL_STATE_IDLE -> StateData.CallStateStatus.CallState.IDLE
            TelephonyManager.CALL_STATE_RINGING -> StateData.CallStateStatus.CallState.RINGING
            TelephonyManager.CALL_STATE_OFFHOOK -> StateData.CallStateStatus.CallState.OFFHOOK
            else -> StateData.CallStateStatus.CallState.UNKNOWN
        }
        return StateData.CallStateStatus(
            state = mappedState,
            isInCall = (mappedState == StateData.CallStateStatus.CallState.OFFHOOK || mappedState == StateData.CallStateStatus.CallState.RINGING)
        )
    }
}
