package com.ksensor.plugins.sensors.positioning.locationstatus

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JvmLocationStatusPlugin : LocationStatusPlugin {
    override val currentState: KSensorResponse<StateData.LocationStatus>
        get() = KSensorResponse(StateData.LocationStatus(false))
    override fun observe(): Flow<KSensorResponse<StateData.LocationStatus>> = emptyFlow()
}

actual fun createLocationStatusPlugin(): LocationStatusPlugin = JvmLocationStatusPlugin()
