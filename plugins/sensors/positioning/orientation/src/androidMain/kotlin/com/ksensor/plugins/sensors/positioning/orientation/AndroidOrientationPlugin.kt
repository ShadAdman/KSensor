package com.ksensor.plugins.sensors.positioning.orientation

import android.content.Context
import android.view.OrientationEventListener
import com.ksensor.core.SensorConfig
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.DeviceOrientation
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

class AndroidOrientationPlugin : OrientationPlugin {
    private val context: Context by lazy { KSensorContext.get() }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val flows = ConcurrentHashMap<SensorConfig, Flow<KSensorResponse<SensorData.Orientation>>>()

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Orientation>> =
        flows.getOrPut(config) {
            callbackFlow {
                val listener = object : OrientationEventListener(context) {
                    override fun onOrientationChanged(orientation: Int) {
                        val mapped = when (orientation) {
                            in 45..134 -> DeviceOrientation.LANDSCAPE
                            in 135..224 -> DeviceOrientation.PORTRAIT
                            in 225..314 -> DeviceOrientation.LANDSCAPE
                            in 315..360, in 0..44 -> DeviceOrientation.PORTRAIT
                            else -> DeviceOrientation.UNKNOWN
                        }
                        val data = SensorData.Orientation(mapped, orientation)
                        trySend(KSensorResponse(data))
                    }
                }
                listener.enable()
                awaitClose { listener.disable() }
            }.shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)
        }
}

actual fun createOrientationPlugin(): OrientationPlugin = AndroidOrientationPlugin()
