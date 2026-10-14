package com.ksensor.plugins.sensors.interaction

import com.ksensor.core.model.StateData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ScrollIdleDetectionTest {

    private fun position(x: Float, y: Float) = StateData.ScrollStatus(x, y, isScrolling = true)

    @Test
    fun emitsScrollingThenIdleWithLastPosition() = runTest {
        val positions = MutableSharedFlow<StateData.ScrollStatus>()
        val emitted = mutableListOf<StateData.ScrollStatus>()
        val job = launch { positions.withIdleDetection(idleTimeoutMs = 300).collect { emitted += it } }
        runCurrent()

        positions.emit(position(0f, 120f))
        runCurrent()
        assertEquals(listOf(StateData.ScrollStatus(0f, 120f, isScrolling = true)), emitted)

        advanceTimeBy(301)
        assertEquals(StateData.ScrollStatus(0f, 120f, isScrolling = false), emitted.last())
        assertEquals(2, emitted.size)

        job.cancel()
    }

    @Test
    fun staysScrollingWhilePositionsKeepArriving() = runTest {
        val positions = MutableSharedFlow<StateData.ScrollStatus>()
        val emitted = mutableListOf<StateData.ScrollStatus>()
        val job = launch { positions.withIdleDetection(idleTimeoutMs = 300).collect { emitted += it } }
        runCurrent()

        positions.emit(position(10f, 0f))
        advanceTimeBy(100)
        positions.emit(position(40f, 0f))
        advanceTimeBy(100)
        positions.emit(position(90f, 0f))
        advanceTimeBy(100)
        runCurrent()

        // No idle emission yet: every gap was shorter than the timeout.
        assertEquals(listOf(10f, 40f, 90f), emitted.map { it.horizontalPosition })
        assertEquals(true, emitted.all { it.isScrolling })

        advanceTimeBy(301)
        assertEquals(StateData.ScrollStatus(90f, 0f, isScrolling = false), emitted.last())

        job.cancel()
    }
}
