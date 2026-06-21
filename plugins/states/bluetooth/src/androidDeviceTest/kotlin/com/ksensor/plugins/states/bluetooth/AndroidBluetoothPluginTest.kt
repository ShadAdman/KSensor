package com.ksensor.plugins.states.bluetooth

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidBluetoothPluginTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun testConnections() = runBlocking {
        val plugin = AndroidBluetoothPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.connections().observe().first()
        }
        assertNotNull(data)
    }

    @Test
    fun testDiscoveries() = runBlocking {
        val plugin = AndroidBluetoothPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.discoveries().observe().first()
        }
    }
}
