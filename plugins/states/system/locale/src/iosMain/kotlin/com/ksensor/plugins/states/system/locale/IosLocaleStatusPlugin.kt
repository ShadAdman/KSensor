package com.ksensor.plugins.states.system.locale

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosLocaleStatusPlugin : LocaleStatusPlugin {
    private val receiver = LocaleReceiver {}

    override val currentState: KSensorResponse<StateData.LocaleStatus>
        get() = KSensorResponse(receiver.getCurrentLocale())

    override fun observe(): Flow<KSensorResponse<StateData.LocaleStatus>> = callbackFlow {
        val obs = LocaleReceiver { trySend(KSensorResponse(it)) }
        obs.register()
        awaitClose { obs.unregister() }
    }
}

actual fun createLocaleStatusPlugin(): LocaleStatusPlugin = IosLocaleStatusPlugin()
