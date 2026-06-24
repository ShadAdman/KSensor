package com.ksensor.plugins.states.system

import com.ksensor.core.Permission
import com.ksensor.core.PluginId
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeSystemPlugin : SystemPlugin {
    override val id: PluginId = PluginId.SYSTEM
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun battery(): StatePlugin<StateData.BatteryStatus> = object : StatePlugin<StateData.BatteryStatus> {
        override val id: PluginId = PluginId.SYSTEM
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.BatteryStatus> = TODO()
        override fun observe(): Flow<KSensorResponse<StateData.BatteryStatus>> = 
            MutableSharedFlow<KSensorResponse<StateData.BatteryStatus>>().asTrackedFlow("battery")
    }

    override fun volume(): StatePlugin<StateData.VolumeStatus> = object : StatePlugin<StateData.VolumeStatus> {
        override val id: PluginId = PluginId.SYSTEM
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.VolumeStatus> = TODO()
        override fun observe(): Flow<KSensorResponse<StateData.VolumeStatus>> = 
            MutableSharedFlow<KSensorResponse<StateData.VolumeStatus>>().asTrackedFlow("volume")
    }

    override fun locale(): StatePlugin<StateData.LocaleStatus> = object : StatePlugin<StateData.LocaleStatus> {
        override val id: PluginId = PluginId.SYSTEM
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.LocaleStatus> = TODO()
        override fun observe(): Flow<KSensorResponse<StateData.LocaleStatus>> = 
            MutableSharedFlow<KSensorResponse<StateData.LocaleStatus>>().asTrackedFlow("locale")
    }

    override fun screen(): StatePlugin<StateData.ScreenStatus> = object : StatePlugin<StateData.ScreenStatus> {
        override val id: PluginId = PluginId.SYSTEM
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.ScreenStatus> = TODO()
        override fun observe(): Flow<KSensorResponse<StateData.ScreenStatus>> = 
            MutableSharedFlow<KSensorResponse<StateData.ScreenStatus>>().asTrackedFlow("screen")
    }

    override fun lock(): StatePlugin<StateData.LockStatus> = object : StatePlugin<StateData.LockStatus> {
        override val id: PluginId = PluginId.SYSTEM
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: KSensorResponse<StateData.LockStatus> = TODO()
        override fun observe(): Flow<KSensorResponse<StateData.LockStatus>> = 
            MutableSharedFlow<KSensorResponse<StateData.LockStatus>>().asTrackedFlow("lock")
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
