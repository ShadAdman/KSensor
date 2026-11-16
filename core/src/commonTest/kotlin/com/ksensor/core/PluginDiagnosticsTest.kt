package com.ksensor.core

import com.ksensor.core.model.KSensorResponse
import com.ksensor.core.model.PluginId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PluginDiagnosticsTest {

    private class TestPlugin(
        override val id: PluginId = PluginId.ACCELEROMETER,
        override val requiredPermissions: List<Permission> = emptyList(),
        override val isSupported: Boolean = true,
        override val unsupportedReason: String? = null
    ) : SensorPlugin<String> {
        override fun observe(config: SensorConfig): Flow<KSensorResponse<String>> {
            return flowOf(KSensorResponse("data")).trackPluginDiagnostics(id)
        }
    }

    private class FailingPlugin(
        override val id: PluginId = PluginId.GYROSCOPE,
        override val requiredPermissions: List<Permission> = emptyList()
    ) : SensorPlugin<String> {
        override fun observe(config: SensorConfig): Flow<KSensorResponse<String>> {
            return flow<KSensorResponse<String>> {
                throw RuntimeException("Hardware sensor error")
            }.trackPluginDiagnostics(id)
        }
    }

    @BeforeTest
    fun setUp() {
        KSensor.getRegisteredPlugins().forEach { KSensor.unregister(it.id) }
    }

    @AfterTest
    fun tearDown() {
        KSensor.getRegisteredPlugins().forEach { KSensor.unregister(it.id) }
    }

    @Test
    fun testRegisterPluginEmitsInitialDiagnostics() {
        val plugin = TestPlugin(id = PluginId.ACCELEROMETER)
        KSensor.register(plugin)

        val diag = KSensor.diagnostics(PluginId.ACCELEROMETER)
        assertNotNull(diag)
        assertEquals(PluginId.ACCELEROMETER, diag.pluginId)
        assertEquals(PluginStatus.READY, diag.status)
        assertTrue(diag.isPlatformAvailable)
        assertTrue(diag.isHealthy)
        assertNull(diag.lastErrorMessage)
    }

    @Test
    fun testUnsupportedPluginEmitsUnsupportedStatus() {
        val plugin = TestPlugin(
            id = PluginId.BAROMETER,
            isSupported = false,
            unsupportedReason = "Barometer not present on device"
        )
        KSensor.register(plugin)

        val diag = KSensor.diagnostics(PluginId.BAROMETER)
        assertNotNull(diag)
        assertEquals(PluginStatus.UNSUPPORTED, diag.status)
        assertFalse(diag.isPlatformAvailable)
        assertFalse(diag.isHealthy)
        assertEquals("Barometer not present on device", diag.unsupportedReason)
    }

    @Test
    fun testFilterStatusAndDiagnosticsMap() {
        val plugin1 = TestPlugin(id = PluginId.ACCELEROMETER)
        val plugin2 = TestPlugin(id = PluginId.BAROMETER, isSupported = false, unsupportedReason = "No hw")
        KSensor.register(plugin1)
        KSensor.register(plugin2)

        val readyPlugins = KSensor.filterStatus(PluginStatus.READY)
        assertEquals(1, readyPlugins.size)
        assertEquals(PluginId.ACCELEROMETER, readyPlugins.first().pluginId)

        val unsupportedPlugins = KSensor.filterStatus(PluginStatus.UNSUPPORTED)
        assertEquals(1, unsupportedPlugins.size)
        assertEquals(PluginId.BAROMETER, unsupportedPlugins.first().pluginId)
    }

    @Test
    fun testTrackPluginDiagnosticsEmitsActiveAndReady() = runTest {
        val plugin = TestPlugin(id = PluginId.LIGHT)
        KSensor.register(plugin)

        assertEquals(PluginStatus.READY, KSensor.diagnostics(PluginId.LIGHT)?.status)

        plugin.observe().collect()

        assertEquals(PluginStatus.READY, KSensor.diagnostics(PluginId.LIGHT)?.status)
    }

    @Test
    fun testFailingPluginEmitsErrorStatus() = runTest {
        val plugin = FailingPlugin(id = PluginId.GYROSCOPE)
        KSensor.register(plugin)

        try {
            plugin.observe().collect()
        } catch (e: Exception) {
            // Expected
        }

        val diag = KSensor.diagnostics(PluginId.GYROSCOPE)
        assertNotNull(diag)
        assertEquals(PluginStatus.ERROR, diag.status)
        assertEquals("Hardware sensor error", diag.lastErrorMessage)
        assertFalse(diag.isHealthy)
    }

    @Test
    fun testGlobalDiagnosticsObserverReceivesUpdates() {
        val receivedUpdates = mutableListOf<PluginDiagnostics>()
        val observer = PluginDiagnosticsObserver { diag ->
            receivedUpdates.add(diag)
        }
        KSensor.addDiagnosticsObserver(observer)

        val plugin = TestPlugin(id = PluginId.PROXIMITY)
        KSensor.register(plugin)

        assertTrue(receivedUpdates.isNotEmpty())
        assertEquals(PluginId.PROXIMITY, receivedUpdates.last().pluginId)
        assertEquals(PluginStatus.READY, receivedUpdates.last().status)

        KSensor.removeDiagnosticsObserver(observer)
    }

    @Test
    fun testUnregisterRemovesDiagnostics() {
        val plugin = TestPlugin(id = PluginId.NOISE)
        KSensor.register(plugin)

        assertNotNull(KSensor.diagnostics(PluginId.NOISE))

        KSensor.unregister(PluginId.NOISE)

        assertNull(KSensor.diagnostics(PluginId.NOISE))
    }
}
