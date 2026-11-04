package com.ksensor.plugins.states.system.brightness

import android.content.Context
import android.provider.Settings
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

class AndroidBrightnessStatusPlugin : BrightnessStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.BrightnessStatus>
        get() = KSensorResponse(BrightnessObserver.getCurrentStatus(context))

    private val brightnessFlow by lazy {
        callbackFlow {
            val observer = BrightnessObserver(context) { trySend(KSensorResponse(it)) }
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                observer
            )
            trySend(KSensorResponse(BrightnessObserver.getCurrentStatus(context)))
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.BrightnessStatus>> = brightnessFlow
}

actual fun createBrightnessStatusPlugin(): BrightnessStatusPlugin = AndroidBrightnessStatusPlugin()
