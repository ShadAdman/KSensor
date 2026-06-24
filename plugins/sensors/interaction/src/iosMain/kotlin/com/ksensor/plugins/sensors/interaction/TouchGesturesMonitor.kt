package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.model.SensorData
import com.ksensor.core.model.TouchGestureType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.*
import kotlin.concurrent.Volatile

internal object TouchGesturesMonitor {

    @Volatile
    private var observer: ((SensorData.TouchGestures) -> Unit)? = null

    private var window: TouchInterceptingWindow? = null

    fun registerObserver(onData: (SensorData.TouchGestures) -> Unit) {
        observer = onData
        installWindowIfNeeded()
    }

    fun removeObserver() {
        observer = null
    }

    internal fun dispatch(data: SensorData.TouchGestures) {
        observer?.invoke(data)
    }

    private fun installWindowIfNeeded() {
        if (window != null) return

        val scene = UIApplication.sharedApplication
            .connectedScenes
            .firstOrNull() as? UIWindowScene ?: return

        window = TouchInterceptingWindow(scene).apply {
            rootViewController = UIViewController()
            makeKeyAndVisible()
        }
    }
}

internal class TouchInterceptingWindow(
    scene: UIWindowScene
) : UIWindow(scene) {

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        handleTouches(touches, TouchGestureType.ACTION_DOWN)
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesMoved(touches, withEvent)
        handleTouches(touches, TouchGestureType.ACTION_MOVE)
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)
        handleTouches(touches, TouchGestureType.ACTION_UP)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun handleTouches(touches: Set<*>, type: TouchGestureType) {
        val touch = touches.firstOrNull() as? UITouch ?: return
        val point = touch.locationInView(this)

        point.useContents {
            TouchGesturesMonitor.dispatch(
                SensorData.TouchGestures(x.toFloat(), y.toFloat(), type)
            )
        }
    }
}
