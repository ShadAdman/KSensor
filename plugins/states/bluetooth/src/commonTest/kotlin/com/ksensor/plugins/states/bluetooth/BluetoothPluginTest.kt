package com.ksensor.plugins.states.bluetooth

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

class FakeBluetoothPlugin : BluetoothPlugin {
    override val id: String = "fake.bluetooth"
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun connections(): StatePlugin<StateData.BleConnectionStatus> = object : StatePlugin<StateData.BleConnectionStatus> {
        override val id: String = "fake.connections"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.BleConnectionStatus = StateData.BleConnectionStatus(emptyList())
        override fun observe(): Flow<StateData.BleConnectionStatus> = 
            MutableSharedFlow<StateData.BleConnectionStatus>().asTrackedFlow("connections")
    }

    override fun discoveries(): StatePlugin<StateData.BleDiscoversStatus> = object : StatePlugin<StateData.BleDiscoversStatus> {
        override val id: String = "fake.discoveries"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.BleDiscoversStatus = StateData.BleDiscoversStatus(emptyList())
        override fun observe(): Flow<StateData.BleDiscoversStatus> = 
            MutableSharedFlow<StateData.BleDiscoversStatus>().asTrackedFlow("discoveries")
    }

    private fun <T> Flow<T>.asTrackedFlow(name: String): Flow<T> {
        return this.onStart { activeObservers.add(name) }
            .onCompletion { activeObservers.remove(name) }
    }
}

class BluetoothPluginTest {

    @Test
    fun testConnections() = runBlocking {
        val fake = FakeBluetoothPlugin()
        val job = launch { fake.connections().observe().collect {} }
        assertTrue(fake.activeObservers.contains("connections"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("connections"))
    }

    @Test
    fun testDiscoveries() = runBlocking {
        val fake = FakeBluetoothPlugin()
        val job = launch { fake.discoveries().observe().collect {} }
        assertTrue(fake.activeObservers.contains("discoveries"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("discoveries"))
    }
}
