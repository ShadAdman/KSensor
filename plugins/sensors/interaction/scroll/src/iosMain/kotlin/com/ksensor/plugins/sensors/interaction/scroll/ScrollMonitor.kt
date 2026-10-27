package com.ksensor.plugins.sensors.interaction.scroll

import com.ksensor.core.model.StateData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CADisplayLink
import platform.UIKit.UIApplication
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
internal object ScrollMonitor {
    private const val RESCAN_INTERVAL_FRAMES = 30

    private var observer: ((StateData.ScrollStatus) -> Unit)? = null
    private var displayLink: CADisplayLink? = null
    private val frameTarget = FrameTarget()
    private var framesUntilRescan = 0
    private var lastOffsets: Map<UIScrollView, Pair<Double, Double>> = emptyMap()

    fun registerObserver(onData: (StateData.ScrollStatus) -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            observer = onData
            if (displayLink != null) return@dispatch_async

            framesUntilRescan = 0
            displayLink = CADisplayLink.displayLinkWithTarget(frameTarget, NSSelectorFromString("onFrame")).apply {
                addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoopCommonModes)
            }
        }
    }

    fun removeObserver() {
        dispatch_async(dispatch_get_main_queue()) {
            observer = null
            displayLink?.invalidate()
            displayLink = null
            lastOffsets = emptyMap()
        }
    }

    internal fun onFrame() {
        if (framesUntilRescan-- <= 0) {
            rescan()
            framesUntilRescan = RESCAN_INTERVAL_FRAMES
        }

        var scrolled: Pair<Double, Double>? = null
        lastOffsets = lastOffsets.mapValues { (scrollView, previous) ->
            val current = scrollView.normalizedOffset()
            if (scrolled == null && current != previous) scrolled = current
            current
        }

        scrolled?.let { (x, y) ->
            observer?.invoke(StateData.ScrollStatus(x.toFloat(), y.toFloat(), isScrolling = true))
        }
    }

    private fun rescan() {
        val found = mutableListOf<UIScrollView>()
        UIApplication.sharedApplication.connectedScenes.forEach { scene ->
            (scene as? UIWindowScene)?.windows?.forEach { window ->
                (window as? UIView)?.let { collectScrollViews(it, found) }
            }
        }
        lastOffsets = found.associateWith { lastOffsets[it] ?: it.normalizedOffset() }
    }

    private fun collectScrollViews(view: UIView, into: MutableList<UIScrollView>) {
        if (view is UIScrollView) into.add(view)
        view.subviews.forEach { (it as? UIView)?.let { child -> collectScrollViews(child, into) } }
    }

    private fun UIScrollView.normalizedOffset(): Pair<Double, Double> {
        val (insetLeft, insetTop) = adjustedContentInset.useContents { left to top }
        return contentOffset.useContents { (x + insetLeft) to (y + insetTop) }
    }
}

@OptIn(BetaInteropApi::class)
private class FrameTarget : NSObject() {
    @ObjCAction
    fun onFrame() {
        ScrollMonitor.onFrame()
    }
}
