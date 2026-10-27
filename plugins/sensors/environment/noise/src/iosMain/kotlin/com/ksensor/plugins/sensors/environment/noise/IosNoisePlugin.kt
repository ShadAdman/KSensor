package com.ksensor.plugins.sensors.environment.noise

import com.ksensor.core.SensorConfig
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.SensorData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import kotlin.time.Duration.Companion.milliseconds

class IosNoisePlugin : NoisePlugin {
    override fun observe(config: SensorConfig): Flow<KSensorResponse<SensorData.Noise>> = noise(config, 80f)

    @OptIn(ExperimentalForeignApi::class)
    override fun noise(config: SensorConfig, loudThresholdDb: Float): Flow<KSensorResponse<SensorData.Noise>> = flow {
        var recorder: AVAudioRecorder? = null
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryRecord, null)
            session.setMode(AVAudioSessionModeMeasurement, null)
            session.setActive(true, null)

            val url = NSURL.fileURLWithPath(NSTemporaryDirectory() + NSUUID().UUIDString() + ".m4a")

            val settings = mapOf<Any?, Any>(
                platform.AVFAudio.AVFormatIDKey to platform.CoreAudioTypes.kAudioFormatMPEG4AAC,
                platform.AVFAudio.AVSampleRateKey to 44100.0,
                platform.AVFAudio.AVNumberOfChannelsKey to 1
            )

            recorder = AVAudioRecorder(url, settings, null)
            recorder.meteringEnabled = true
            recorder.prepareToRecord()
            recorder.record()

            while (true) {
                recorder.updateMeters()

                val peakPower = recorder.peakPowerForChannel(0u)
                val db = (peakPower + 100).coerceAtLeast(0f)

                emit(
                    KSensorResponse(
                        data = SensorData.Noise(
                            dB = db,
                            isLoud = db > loudThresholdDb,
                            timestamp = null
                        )
                    )
                )

                delay(config.intervalMs.milliseconds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder?.stop()
            recorder?.deleteRecording()
            try {
                AVAudioSession.sharedInstance().setActive(false, null)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

actual fun createNoisePlugin(): NoisePlugin = IosNoisePlugin()
