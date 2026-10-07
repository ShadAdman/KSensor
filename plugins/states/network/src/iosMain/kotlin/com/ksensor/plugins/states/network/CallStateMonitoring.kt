package com.ksensor.plugins.states.network

import com.ksensor.core.model.StateData
import platform.CallKit.CXCall
import platform.CallKit.CXCallObserver
import platform.CallKit.CXCallObserverDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

internal class CallStateMonitoring(
    private val onCallStateChanged: (StateData.CallStateStatus) -> Unit,
) {
    private val callObserver = CXCallObserver()
    private var delegate: CXCallObserverDelegateProtocol? = null

    fun startMonitoring() {
        val delegateInstance = object : NSObject(), CXCallObserverDelegateProtocol {
            override fun callObserver(callObserver: CXCallObserver, callChanged: CXCall) {
                onCallStateChanged(getCallStatus(callObserver))
            }
        }
        delegate = delegateInstance
        callObserver.setDelegate(delegateInstance, queue = dispatch_get_main_queue())
        onCallStateChanged(getCallStatus(callObserver))
    }

    fun stopMonitoring() {
        callObserver.setDelegate(null, queue = null)
        delegate = null
    }

    fun getCurrentStatus(): StateData.CallStateStatus {
        return getCallStatus(CXCallObserver())
    }

    private fun getCallStatus(observer: CXCallObserver): StateData.CallStateStatus {
        @Suppress("UNCHECKED_CAST")
        val calls = (observer.calls as? List<CXCall>) ?: emptyList()
        val activeCalls = calls.filter { !it.hasEnded }

        val state = when {
            activeCalls.isEmpty() -> StateData.CallStateStatus.CallState.IDLE
            activeCalls.any { it.hasConnected } -> StateData.CallStateStatus.CallState.OFFHOOK
            activeCalls.any { !it.hasConnected } -> StateData.CallStateStatus.CallState.RINGING
            else -> StateData.CallStateStatus.CallState.UNKNOWN
        }

        return StateData.CallStateStatus(
            state = state,
            isInCall = (state == StateData.CallStateStatus.CallState.OFFHOOK || state == StateData.CallStateStatus.CallState.RINGING)
        )
    }
}
