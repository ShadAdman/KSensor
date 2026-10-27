package com.ksensor.plugins.states.lifecycle.appvisibility

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
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

class AndroidAppVisibilityStatusPlugin : AppVisibilityStatusPlugin {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.AppVisibilityStatus>
        get() = KSensorResponse(StateData.AppVisibilityStatus(true))

    private val appVisibilityFlow by lazy {
        callbackFlow {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> trySend(KSensorResponse(StateData.AppVisibilityStatus(true)))
                    Lifecycle.Event.ON_STOP -> trySend(KSensorResponse(StateData.AppVisibilityStatus(false)))
                    else -> Unit
                }
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
            awaitClose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.AppVisibilityStatus>> = appVisibilityFlow
}

actual fun createAppVisibilityStatusPlugin(): AppVisibilityStatusPlugin = AndroidAppVisibilityStatusPlugin()
