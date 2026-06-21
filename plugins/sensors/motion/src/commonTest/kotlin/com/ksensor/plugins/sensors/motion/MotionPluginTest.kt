package com.ksensor.plugins.sensors.motion

import com.ksensor.core.Permission
import com.ksensor.core.SensorConfig
import com.ksensor.core.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeMotionPlugin : MotionPlugin {
    override val id: String = "fake.motion"
    override val requiredPermissions: List<Permission> = emptyList()

    val activeObservers = mutableSetOf<String>()

    override fun accelerometer(config: SensorConfig): Flow<SensorData.Accelerometer> = 
        MutableSharedFlow<SensorData.Accelerometer>().asTrackedFlow("accelerometer")

    override fun gyroscope(config: SensorConfig): Flow<SensorData.Gyroscope> = 
        MutableSharedFlow<SensorData.Gyroscope>().asTrackedFlow("gyroscope")

    override fun stepCounter(config: SensorConfig): Flow<SensorData.StepCounter> = 
        MutableSharedFlow<SensorData.StepCounter>().asTrackedFlow("stepCounter")

    override fun stepDetector(config: SensorConfig): Flow<SensorData.StepDetector> = 
        MutableSharedFlow<SensorData.StepDetector>().asTrackedFlow("stepDetector")

    private fun <T> Flow<T>.asTrackedFlow(name: String): Flow<T> {
        return this.onStart { activeObservers.add(name) }
            .onCompletion { activeObservers.remove(name) }
    }
}

class MotionPluginTest {

    @Test
    fun testAccelerometer() = runBlocking {
        val fake = FakeMotionPlugin()
        val job = launch { fake.accelerometer().collect {} }
        assertTrue(fake.activeObservers.contains("accelerometer"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("accelerometer"))
    }

    @Test
    fun testGyroscope() = runBlocking {
        val fake = FakeMotionPlugin()
        val job = launch { fake.gyroscope().collect {} }
        assertTrue(fake.activeObservers.contains("gyroscope"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("gyroscope"))
    }

    @Test
    fun testStepCounter() = runBlocking {
        val fake = FakeMotionPlugin()
        val job = launch { fake.stepCounter().collect {} }
        assertTrue(fake.activeObservers.contains("stepCounter"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("stepCounter"))
    }

    @Test
    fun testStepDetector() = runBlocking {
        val fake = FakeMotionPlugin()
        val job = launch { fake.stepDetector().collect {} }
        assertTrue(fake.activeObservers.contains("stepDetector"))
        job.cancelAndJoin()
        assertFalse(fake.activeObservers.contains("stepDetector"))
    }
}
