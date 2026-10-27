package com.ksensor.plugins.sensors.interaction.scroll

import com.ksensor.core.model.StateData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal const val SCROLL_IDLE_TIMEOUT_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class)
internal fun Flow<StateData.ScrollStatus>.withIdleDetection(
    idleTimeoutMs: Long = SCROLL_IDLE_TIMEOUT_MS
): Flow<StateData.ScrollStatus> = transformLatest { status ->
    emit(status.copy(isScrolling = true))
    delay(idleTimeoutMs)
    emit(status.copy(isScrolling = false))
}
