package com.ksensor.plugins.states.lifecycle.appvisibility

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmAppVisibilityStatusPlugin : AppVisibilityStatusPlugin {
    override val currentState: KSensorResponse<StateData.AppVisibilityStatus>
        get() = KSensorResponse(StateData.AppVisibilityStatus(true))

    override fun observe(): Flow<KSensorResponse<StateData.AppVisibilityStatus>> = emptyFlow()
}

actual fun createAppVisibilityStatusPlugin(): AppVisibilityStatusPlugin = JvmAppVisibilityStatusPlugin()
