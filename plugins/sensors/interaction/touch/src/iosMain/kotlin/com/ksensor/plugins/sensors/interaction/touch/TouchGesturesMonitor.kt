package com.ksensor.plugins.sensors.interaction.touch

import com.ksensor.core.model.SensorData
import com.ksensor.core.model.TouchGestureType
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal object TouchGesturesMonitor {

    private var observer: ((SensorData.TouchGestures) -> Unit)? = null

    fun registerObserver(obs: (SensorData.TouchGestures) -> Unit) {
        this.observer = obs
        attachToAllWindows()
    }

    fun removeObserver() {
        this.observer = null
    }

    private fun attachToAllWindows() {
        val windows = UIApplication.sharedApplication.windows
        for (window in windows) {
            (window as? UIWindow)?.let { attachToView(it) }
        }
    }

    private fun attachToView(view: UIView) {
        val gestureDelegate = GestureDelegate { type, x, y ->
            observer?.invoke(SensorData.TouchGestures(x, y, type))
        }

        val singleTap = UITapGestureRecognizer().apply {
            numberOfTapsRequired = 1u
            delegate = gestureDelegate
            addTarget(gestureDelegate, NSSelectorFromString("handleSingleTap:"))
        }

        val doubleTap = UITapGestureRecognizer().apply {
            numberOfTapsRequired = 2u
            delegate = gestureDelegate
            addTarget(gestureDelegate, NSSelectorFromString("handleDoubleTap:"))
        }
        singleTap.requireGestureRecognizerToFail(doubleTap)

        val longPress = UILongPressGestureRecognizer().apply {
            delegate = gestureDelegate
            addTarget(gestureDelegate, NSSelectorFromString("handleLongPress:"))
        }

        view.addGestureRecognizer(singleTap)
        view.addGestureRecognizer(doubleTap)
        view.addGestureRecognizer(longPress)

        for (subview in view.subviews) {
            (subview as? UIView)?.let { attachToView(it) }
        }
    }

    @OptIn(BetaInteropApi::class)
    private class GestureDelegate(
        private val callback: (TouchGestureType, Float, Float) -> Unit,
    ) : NSObject(), UIGestureRecognizerDelegateProtocol {

        override fun gestureRecognizer(
            gestureRecognizer: UIGestureRecognizer,
            shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer,
        ): Boolean = true

        @ObjCAction
        @Suppress("unused")
        fun handleSingleTap(recognizer: UITapGestureRecognizer) {
            if (recognizer.state == UIGestureRecognizerStateEnded) {
                val point = recognizer.locationInView(recognizer.view)
                point.useContents {
                    callback(TouchGestureType.TAP, x.toFloat(), y.toFloat())
                }
            }
        }

        @ObjCAction
        @Suppress("unused")
        fun handleDoubleTap(recognizer: UITapGestureRecognizer) {
            if (recognizer.state == UIGestureRecognizerStateEnded) {
                val point = recognizer.locationInView(recognizer.view)
                point.useContents {
                    callback(TouchGestureType.DOUBLE_TAP, x.toFloat(), y.toFloat())
                }
            }
        }

        @ObjCAction
        @Suppress("unused")
        fun handleLongPress(recognizer: UILongPressGestureRecognizer) {
            if (recognizer.state == UIGestureRecognizerStateEnded) {
                val point = recognizer.locationInView(recognizer.view)
                point.useContents {
                    callback(TouchGestureType.LONG_PRESS, x.toFloat(), y.toFloat())
                }
            }
        }
    }
}
