package com.ksensor.plugins.states.system

import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeSystemPlugin : SystemPlugin {
    override val id: String = "fake.system"
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun battery(): StatePlugin<StateData.BatteryStatus> = object : StatePlugin<StateData.BatteryStatus> {
        override val id: String = "fake.battery"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.BatteryStatus = StateData.BatteryStatus(100, StateData.BatteryStatus.ChargingState.FULL, null, null)
        override fun observe(): Flow<StateData.BatteryStatus> = 
            MutableSharedFlow<StateData.BatteryStatus>().asTrackedFlow("battery")
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = object : StatePlugin<StateData.VolumeStatus> {
        override val id: String = "fake.volume"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.VolumeStatus = StateData.VolumeStatus(50)
        override fun observe(): Flow<StateData.VolumeStatus> = 
            MutableSharedFlow<StateData.VolumeStatus>().asTrackedFlow("volume")
    }

    override fun locale(): StatePlugin<StateData.LocaleStatus> = object : StatePlugin<StateData.LocaleStatus> {
        override val id: String = "fake.locale"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.LocaleStatus = StateData.LocaleStatus("en", "US", "en-US", "English", false)
        override fun observe(): Flow<StateData.LocaleStatus> = 
            MutableSharedFlow<StateData.LocaleStatus>().asTrackedFlow("locale")
    }

    override fun screen(): StatePlugin<StateData.ScreenStatus> = object : StatePlugin<StateData.ScreenStatus> {
        override val id: String = "fake.screen"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.ScreenStatus = StateData.ScreenStatus(true)
        override fun observe(): Flow<StateData.ScreenStatus> = 
            MutableSharedFlow<StateData.ScreenStatus>().asTrackedFlow("screen")
    }

    override fun lock(): StatePlugin<StateData.LockStatus> = object : StatePlugin<StateData.LockStatus> {
        override val id: String = "fake.lock"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.LockStatus = StateData.LockStatus(false)
        override fun observe(): Flow<StateData.LockStatus> = 
            MutableSharedFlow<StateData.LockStatus>().asTrackedFlow("lock")
    }

    private fun <T> Flow<T>.asTrackedFlow(name: String): Flow<T> {
        return this.onStart { activeObservers.add(name) }
            .onCompletion { activeObservers.remove(name) }
    }
}

class SystemPluginTest {

    @Test
    fun testBattery() = runBlocking {
        val fake = FakeSystemPlugin()
        val job = launch { fake.battery().observe().collect {} }
        assertTrue(fake.activeObservers.contains("battery"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("battery"))
    }

    @Test
    fun testVolume() = runBlocking {
        val fake = FakeSystemPlugin()
        val job = launch { fake.volume().observe().collect {} }
        assertTrue(fake.activeObservers.contains("volume"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("volume"))
    }

    @Test
    fun testLocale() = runBlocking {
        val fake = FakeSystemPlugin()
        val job = launch { fake.locale().observe().collect {} }
        assertTrue(fake.activeObservers.contains("locale"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("locale"))
    }

    @Test
    fun testScreen() = runBlocking {
        val fake = FakeSystemPlugin()
        val job = launch { fake.screen().observe().collect {} }
        assertTrue(fake.activeObservers.contains("screen"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("screen"))
    }

    @Test
    fun testLock() = runBlocking {
        val fake = FakeSystemPlugin()
        val job = launch { fake.lock().observe().collect {} }
        assertTrue(fake.activeObservers.contains("lock"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("lock"))
    }
}
