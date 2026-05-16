package org.kmp.ksensor.state

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.darwin.IntVar
import platform.darwin.LongVar
import platform.darwin.dispatch_get_main_queue
import platform.darwin.notify_cancel
import platform.darwin.notify_get_state
import platform.darwin.notify_register_dispatch

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
            ) { token ->
                val state = memScoped {
                    val outState = alloc<LongVar>()
                    notify_get_state(token, outState.ptr)
                    outState.value
                }
                // state == 1 means screen is blanked (OFF)
                // state == 0 means screen is ON
                onStateChanged(state == 0L)
            }
            if (status == 0) {
                registrationToken = outToken.value
                val state = memScoped {
                    val outState = alloc<LongVar>()
                    notify_get_state(registrationToken, outState.ptr)
                    outState.value
                }
                onStateChanged(state == 0L)
            }
        }
    }

    fun unregister() {
        if (registrationToken != 0) {
            notify_cancel(registrationToken)
            registrationToken = 0
        }
    }
}
