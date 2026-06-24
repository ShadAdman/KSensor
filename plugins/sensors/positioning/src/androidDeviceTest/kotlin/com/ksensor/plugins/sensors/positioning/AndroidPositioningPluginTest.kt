package com.ksensor.plugins.sensors.positioning

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidPositioningPluginTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Test
    fun testLocation() = runBlocking {
        val plugin = AndroidPositioningPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.location().first()
        }
    }

    @Test
    fun testMagnetometer() = runBlocking {
        val plugin = AndroidPositioningPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.magnetometer().first()
        }
    }

    @Test
    fun testOrientation() = runBlocking {
        val plugin = AndroidPositioningPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.orientation().first()
        }
    }

    @Test
    fun testLocationStatus() = runBlocking {
        val plugin = AndroidPositioningPlugin()
        val data = withTimeoutOrNull(2000) {
            plugin.locationStatus().observe().first()
        }
    }
}
