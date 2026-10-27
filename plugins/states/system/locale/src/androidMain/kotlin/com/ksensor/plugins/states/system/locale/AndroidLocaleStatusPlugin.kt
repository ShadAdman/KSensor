package com.ksensor.plugins.states.system.locale

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

class AndroidLocaleStatusPlugin : LocaleStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val receiver = LocaleReceiver(context) {}

    override val currentState: KSensorResponse<StateData.LocaleStatus>
        get() = KSensorResponse(receiver.getCurrentLocale())

    private val localeFlow by lazy {
        callbackFlow {
            val obs = LocaleReceiver(context) { trySend(KSensorResponse(it)) }
            context.registerReceiver(obs, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
            awaitClose { context.unregisterReceiver(obs) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.LocaleStatus>> = localeFlow
}

actual fun createLocaleStatusPlugin(): LocaleStatusPlugin = AndroidLocaleStatusPlugin()
