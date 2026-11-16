package com.ksensor.plugins.sensors.environment.barometer

import com.ksensor.core.KSensor
import com.ksensor.core.PluginStatus
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import com.ksensor.core.trackPluginDiagnostics
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMAltimeter
import platform.Foundation.NSOperationQueue

class IosBarometerPlugin : BarometerPlugin {
    override val isSupported: Boolean get() = CMAltimeter.isRelativeAltitudeAvailable()
    override val unsupportedReason: String? get() = if (!isSupported) "Barometer altimeter is not supported on this iOS device" else null

    private val altimeter = if (CMAltimeter.isRelativeAltitudeAvailable()) CMAltimeter() else null

    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Barometer>> = callbackFlow {
        if (altimeter == null) {
            KSensor.reportStatus(
                pluginId = id,
                status = PluginStatus.UNSUPPORTED,
                unsupportedReason = unsupportedReason
            )
            close()
            return@callbackFlow
        }
        altimeter.startRelativeAltitudeUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
            data?.let {
                val sensorData = SensorData.Barometer(it.pressure.doubleValue.toFloat())
                trySend(KSensorResponse(sensorData))
            }
        }
        awaitClose { altimeter.stopRelativeAltitudeUpdates() }
    }.trackPluginDiagnostics(id)
}

actual fun createBarometerPlugin(): BarometerPlugin = IosBarometerPlugin()
