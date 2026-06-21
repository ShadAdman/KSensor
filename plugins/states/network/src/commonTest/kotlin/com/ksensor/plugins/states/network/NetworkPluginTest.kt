package com.ksensor.plugins.states.network

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

class FakeNetworkPlugin : NetworkPlugin {
    override val id: String = "fake.network"
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun connectivity(): StatePlugin<StateData.ConnectivityStatus> = object : StatePlugin<StateData.ConnectivityStatus> {
        override val id: String = "fake.connectivity"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.ConnectivityStatus = StateData.ConnectivityStatus(true)
        override fun observe(): Flow<StateData.ConnectivityStatus> = 
            MutableSharedFlow<StateData.ConnectivityStatus>().asTrackedFlow("connectivity")
    }

    override fun activeNetwork(): StatePlugin<StateData.CurrentActiveNetwork> = object : StatePlugin<StateData.CurrentActiveNetwork> {
        override val id: String = "fake.activeNetwork"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.CurrentActiveNetwork = StateData.CurrentActiveNetwork(StateData.CurrentActiveNetwork.ActiveNetwork.WIFI)
        override fun observe(): Flow<StateData.CurrentActiveNetwork> = 
            MutableSharedFlow<StateData.CurrentActiveNetwork>().asTrackedFlow("activeNetwork")
    }

    private fun <T> Flow<T>.asTrackedFlow(name: String): Flow<T> {
        return this.onStart { activeObservers.add(name) }
            .onCompletion { activeObservers.remove(name) }
    }
}

class NetworkPluginTest {

    @Test
    fun testConnectivity() = runBlocking {
        val fake = FakeNetworkPlugin()
        val job = launch { fake.connectivity().observe().collect {} }
        assertTrue(fake.activeObservers.contains("connectivity"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("connectivity"))
    }

    @Test
    fun testActiveNetwork() = runBlocking {
        val fake = FakeNetworkPlugin()
        val job = launch { fake.activeNetwork().observe().collect {} }
        assertTrue(fake.activeObservers.contains("activeNetwork"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("activeNetwork"))
    }
}
