package com.ksensor.plugins.states.system

import kotlinx.cinterop.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.notify_cancel
import platform.darwin.notify_get_state
import platform.darwin.notify_register_dispatch

@OptIn(ExperimentalForeignApi::class)
internal class ScreenStateReceiver(private val onStateChanged: (Boolean) -> Unit) {
    private var registrationToken: Int = 0

    fun register() {
        if (registrationToken != 0) return

        memScoped {
            val outToken = alloc<IntVar>()
            val status = notify_register_dispatch(
                "com.apple.springboard.hasBlankedScreen",
                outToken.ptr,
                dispatch_get_main_queue()
            ) { token: Int ->
                val state = memScoped {
                    val outState = alloc<ULongVar>()
                    notify_get_state(token, outState.ptr)
                    outState.value
                }
                // state == 1 means screen is blanked (OFF)
                // state == 0 means screen is ON
                onStateChanged(state == 0uL)
            }
            if (status == 0u) {
                registrationToken = outToken.value
                val state = memScoped {
                    val outState = alloc<ULongVar>()
                    notify_get_state(registrationToken, outState.ptr)
                    outState.value
                }
                onStateChanged(state == 0uL)
            }
        }
    }

    fun unregister() {
        if (registrationToken != 0) {
            notify_cancel(registrationToken)
            registrationToken = 0
        }
    }

    fun isScreenOn(): Boolean {
        // Approximate if not registered
        return true
    }
}
