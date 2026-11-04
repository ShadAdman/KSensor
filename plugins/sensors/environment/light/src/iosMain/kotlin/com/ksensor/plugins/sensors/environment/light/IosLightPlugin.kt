package com.ksensor.plugins.sensors.environment.light

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSTimer
import platform.UIKit.UIScreen

class IosLightPlugin : LightPlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.LightIlluminance>> = callbackFlow {
        val timer = NSTimer.scheduledTimerWithTimeInterval(
            config.intervalMs / 1000.0,
            repeats = true,
            block = {
                val brightness = UIScreen.mainScreen.brightness.toFloat()
                val sensorData = SensorData.LightIlluminance(brightness * 1000f)
                trySend(KSensorResponse(sensorData))
            }
        )
        NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes)
        awaitClose { timer.invalidate() }
    }
}

actual fun createLightPlugin(): LightPlugin = IosLightPlugin()
