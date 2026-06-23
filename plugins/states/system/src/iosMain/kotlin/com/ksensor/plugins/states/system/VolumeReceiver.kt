package com.ksensor.plugins.states.system

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.outputVolume
import platform.AVFAudio.setActive
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.Foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
internal class VolumeReceiver(private val onVolumeChange: (Int) -> Unit) {
    private val session = AVAudioSession.sharedInstance()
    private var observer: VolumeObserver? = null
    private val volumeKey = "outputVolume"

    private inner class VolumeObserver : NSObject(), NSKeyValueObservingProtocol {
        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?
        ) {
            if (keyPath == volumeKey) {
                val volume = change?.get(NSKeyValueChangeNewKey) as? Float ?: return
                onVolumeChange((volume * 100).toInt())
            }
        }
    }

    init {
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        session.setActive(true, error = null)
    }

    fun register() {
        val newObserver = VolumeObserver()
        observer = newObserver
        session.addObserver(
            observer = newObserver,
            forKeyPath = volumeKey,
            options = NSKeyValueObservingOptionNew,
            context = null
        )
    }

    fun unregister() {
        observer?.let {
            session.removeObserver(it, volumeKey)
            observer = null
        }
    }

    fun getCurrentVolume(): Int {
        return (session.outputVolume * 100).toInt()
    }
}
