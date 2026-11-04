package com.ksensor.plugins.states.system.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
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

class AndroidVolumeStatusPlugin : VolumeStatusPlugin {
    private val context: Context by lazy { KSensorContext.get() }
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val currentState: KSensorResponse<StateData.VolumeStatus>
        get() = KSensorResponse(StateData.VolumeStatus(audioManager.getVolumePercentage(STREAM_TYPE)))

    private val volumeFlow by lazy {
        callbackFlow {
            val receiver = VolumeReceiver(audioManager) { trySend(KSensorResponse(StateData.VolumeStatus(it))) }
            context.registerReceiver(receiver, IntentFilter(VOLUME_CHANGED_ACTION))
            trySend(KSensorResponse(StateData.VolumeStatus(audioManager.getVolumePercentage(STREAM_TYPE))))
            awaitClose { context.unregisterReceiver(receiver) }
        }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
    }

    override fun observe(): Flow<KSensorResponse<StateData.VolumeStatus>> = volumeFlow
}

actual fun createVolumeStatusPlugin(): VolumeStatusPlugin = AndroidVolumeStatusPlugin()
