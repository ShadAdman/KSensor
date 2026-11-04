package com.ksensor.plugins.states.system.locale

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmLocaleStatusPlugin : LocaleStatusPlugin {
    override val currentState: KSensorResponse<StateData.LocaleStatus>
        get() = KSensorResponse(StateData.LocaleStatus("en", "US", "en_US", "English (United States)", false))

    override fun observe(): Flow<KSensorResponse<StateData.LocaleStatus>> = emptyFlow()
}

actual fun createLocaleStatusPlugin(): LocaleStatusPlugin = WasmLocaleStatusPlugin()
