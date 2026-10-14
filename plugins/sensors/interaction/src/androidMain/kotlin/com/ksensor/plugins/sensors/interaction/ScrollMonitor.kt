package com.ksensor.plugins.sensors.interaction

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.ksensor.core.context.KSensorContext
import com.ksensor.core.model.StateData
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.WeakHashMap

/**
 * Observes scrolling of View-based containers (ScrollView, HorizontalScrollView, RecyclerView,
 * ListView, WebView, ...) in the currently resumed activity.
 *
 * A single [ViewTreeObserver.OnScrollChangedListener] on the decor view tells us *that* something
 * scrolled; the hierarchy is then scanned to find *which* container moved and read its offset.
 */
internal object ScrollMonitor {
    private val app: Application by lazy { KSensorContext.get().applicationContext as Application }
    private val mainHandler = Handler(Looper.getMainLooper())

    // Offsets are read through computeXxxScrollOffset() so containers that don't move scrollX/scrollY
    // (e.g. RecyclerView, ListView) report their real position. These methods are protected on View.
    private val horizontalOffsetMethod: Method by lazy {
        View::class.java.getDeclaredMethod("computeHorizontalScrollOffset").apply { isAccessible = true }
    }
    private val verticalOffsetMethod: Method by lazy {
        View::class.java.getDeclaredMethod("computeVerticalScrollOffset").apply { isAccessible = true }
    }

    private var observer: ((StateData.ScrollStatus) -> Unit)? = null
    private var attachedDecorView: WeakReference<View>? = null
    private val lastOffsets = WeakHashMap<View, Pair<Int, Int>>()

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val decorView = attachedDecorView?.get() ?: return@OnScrollChangedListener
        val (x, y) = findScrolledOffset(decorView) ?: return@OnScrollChangedListener
        observer?.invoke(StateData.ScrollStatus(x.toFloat(), y.toFloat(), isScrolling = true))
    }

    fun registerObserver(onData: (StateData.ScrollStatus) -> Unit) {
        mainHandler.post {
            observer = onData
            app.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks)
            KSensorContext.getActivity()?.let(::attach)
        }
    }

    fun removeObserver() {
        mainHandler.post {
            observer = null
            app.unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks)
            detach()
        }
    }

    private fun attach(activity: Activity) {
        val decorView = activity.window?.peekDecorView() ?: return
        if (attachedDecorView?.get() === decorView) return

        detach()
        decorView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        attachedDecorView = WeakReference(decorView)
        // Record current offsets so containers that are already scrolled aren't reported as moving.
        findScrolledOffset(decorView)
    }

    private fun detach() {
        attachedDecorView?.get()?.viewTreeObserver?.takeIf { it.isAlive }
            ?.removeOnScrollChangedListener(scrollListener)
        attachedDecorView = null
        lastOffsets.clear()
    }

    /**
     * Updates the cached offset of every view and returns the offset of the first one that changed.
     */
    private fun findScrolledOffset(root: View): Pair<Int, Int>? {
        var scrolled: Pair<Int, Int>? = null
        val pending = ArrayDeque<View>().apply { add(root) }

        while (pending.isNotEmpty()) {
            val view = pending.removeFirst()
            val offset = Pair(
                horizontalOffsetMethod.invoke(view) as Int,
                verticalOffsetMethod.invoke(view) as Int
            )
            val previous = lastOffsets.put(view, offset) ?: Pair(0, 0)
            if (scrolled == null && offset != previous) scrolled = offset

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) pending.add(view.getChildAt(i))
            }
        }
        return scrolled
    }

    private object ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            attach(activity)
        }
        override fun onActivityDestroyed(activity: Activity) {
            if (attachedDecorView?.get() === activity.window?.peekDecorView()) detach()
        }
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
    }
}
