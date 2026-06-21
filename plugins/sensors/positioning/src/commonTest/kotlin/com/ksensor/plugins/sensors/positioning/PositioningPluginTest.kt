package com.ksensor.plugins.sensors.positioning

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.SensorData
import com.ksensor.core.model.StateData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakePositioningPlugin : PositioningPlugin {
    override val id: String = "fake.positioning"
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun location(config: SensorConfig): Flow<SensorData.Location> = 
        MutableSharedFlow<SensorData.Location>().asTrackedFlow("location")

    override fun magnetometer(config: SensorConfig): Flow<SensorData.Magnetometer> = 
        MutableSharedFlow<SensorData.Magnetometer>().asTrackedFlow("magnetometer")

    override fun orientation(config: SensorConfig): Flow<SensorData.Orientation> = 
        MutableSharedFlow<SensorData.Orientation>().asTrackedFlow("orientation")

    override fun locationStatus(): StatePlugin<StateData.LocationStatus> = object : StatePlugin<StateData.LocationStatus> {
        override val id: String = "fake.locationStatus"
        override val requiredPermissions: List<Permission> = emptyList()
        override val currentState: StateData.LocationStatus = StateData.LocationStatus(true)
        override fun observe(): Flow<StateData.LocationStatus> = 
            MutableSharedFlow<StateData.LocationStatus>().asTrackedFlow("locationStatus")
    }

    private fun <T> Flow<T>.asTrackedFlow(name: String): Flow<T> {
        return this.onStart { activeObservers.add(name) }
            .onCompletion { activeObservers.remove(name) }
    }
}

class PositioningPluginTest {

    @Test
    fun testLocation() = runBlocking {
        val fake = FakePositioningPlugin()
        val job = launch { fake.location().collect {} }
        assertTrue(fake.activeObservers.contains("location"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("location"))
    }

    @Test
    fun testMagnetometer() = runBlocking {
        val fake = FakePositioningPlugin()
        val job = launch { fake.magnetometer().collect {} }
        assertTrue(fake.activeObservers.contains("magnetometer"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("magnetometer"))
    }

    @Test
    fun testOrientation() = runBlocking {
        val fake = FakePositioningPlugin()
        val job = launch { fake.orientation().collect {} }
        assertTrue(fake.activeObservers.contains("orientation"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("orientation"))
    }

    @Test
    fun testLocationStatus() = runBlocking {
        val fake = FakePositioningPlugin()
        val job = launch { fake.locationStatus().observe().collect {} }
        assertTrue(fake.activeObservers.contains("locationStatus"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("locationStatus"))
    }
}
