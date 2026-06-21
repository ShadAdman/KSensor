package com.ksensor.plugins.states.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.ksensor.core.Permission
import com.ksensor.core.PlatformType
import com.ksensor.core.PluginId
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLifecyclePlugin : LifecyclePlugin {
    override val id: PluginId = PluginId.LIFECYCLE
    override val requiredPermissions: List<Permission> = emptyList()

    override fun appVisibility(): StatePlugin<StateData.AppVisibilityStatus> = object : StatePlugin<StateData.AppVisibilityStatus> {
        override val id: PluginId = PluginId.LIFECYCLE
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.AppVisibilityStatus>
            get() = KSensorResponse(StateData.AppVisibilityStatus(true), PlatformType.Android) // Placeholder

        override fun observe(): Flow<KSensorResponse<StateData.AppVisibilityStatus>> = callbackFlow {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> trySend(KSensorResponse(StateData.AppVisibilityStatus(true), PlatformType.Android))
                    Lifecycle.Event.ON_STOP -> trySend(KSensorResponse(StateData.AppVisibilityStatus(false), PlatformType.Android))
                    else -> Unit
                }
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
            awaitClose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
        }
    }
}

actual fun createLifecyclePlugin(): LifecyclePlugin = AndroidLifecyclePlugin()
