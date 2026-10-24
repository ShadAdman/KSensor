package com.ksensor.core

import com.ksensor.core.model.PluginId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PermissionManifestGeneratorTest {

    private class MockPlugin(
        override val id: PluginId,
        override val requiredPermissions: List<Permission>,
        override val permissionMetadata: List<PermissionMetadata> = requiredPermissions.flatMap { it.toMetadataList() }
    ) : KSensorPlugin

    @Test
    fun testAndroidManifestGenerationForLocationAndBluetooth() {
        val locationPlugin = MockPlugin(PluginId.POSITIONING, listOf(Permission.LOCATION))
        val bluetoothPlugin = MockPlugin(PluginId.BLUETOOTH, listOf(Permission.BLUETOOTH))

        val manifest = KSensor.permissionManifest(
            plugins = listOf(locationPlugin, bluetoothPlugin),
            includeOptional = true,
            includeComments = false
        )

        assertTrue(manifest.contains("android.permission.ACCESS_FINE_LOCATION"))
        assertTrue(manifest.contains("android.permission.ACCESS_COARSE_LOCATION"))
        assertTrue(manifest.contains("android.permission.BLUETOOTH_SCAN"))
        assertTrue(manifest.contains("android.permission.BLUETOOTH_CONNECT"))
        assertTrue(manifest.contains("android:minSdkVersion=\"31\""))
    }

    @Test
    fun testIosInfoPlistGenerationForLocationAndAudio() {
        val locationPlugin = MockPlugin(PluginId.POSITIONING, listOf(Permission.LOCATION))
        val audioPlugin = MockPlugin(PluginId.ENVIRONMENT, listOf(Permission.RECORD_AUDIO))

        val plist = KSensor.iosInfoPlistEntries(
            plugins = listOf(locationPlugin, audioPlugin),
            includeOptional = false,
            includeComments = false
        )

        assertTrue(plist.contains("<key>NSLocationWhenInUseUsageDescription</key>"))
        assertTrue(plist.contains("<key>NSMicrophoneUsageDescription</key>"))
        // Optional permission should be excluded when includeOptional = false
        assertTrue(!plist.contains("<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>"))
    }

    @Test
    fun testDeduplicationOfPermissions() {
        val plugin1 = MockPlugin(PluginId.POSITIONING, listOf(Permission.LOCATION))
        val plugin2 = MockPlugin(PluginId.MOTION, listOf(Permission.LOCATION))

        val manifest = KSensor.permissionManifest(
            plugins = listOf(plugin1, plugin2),
            includeOptional = true,
            includeComments = false
        )

        val count = "android.permission.ACCESS_FINE_LOCATION".let { target ->
            manifest.split(target).size - 1
        }
        assertEquals(1, count, "Duplicate permission entries should be deduplicated")
    }

    @Test
    fun testEmptyPermissions() {
        val emptyPlugin = MockPlugin(PluginId.LIFECYCLE, emptyList())

        val manifest = KSensor.permissionManifest(
            plugins = listOf(emptyPlugin),
            includeOptional = true,
            includeComments = true
        )

        assertEquals("<!-- No Android permissions required -->", manifest)

        val plist = KSensor.iosInfoPlistEntries(
            plugins = listOf(emptyPlugin),
            includeOptional = true,
            includeComments = true
        )

        assertEquals("<!-- No iOS Info.plist permission entries required -->", plist)
    }

    @Test
    fun testCustomPermissionMetadata() {
        val customMetadata = listOf(
            PermissionMetadata(
                platform = TargetPlatform.ANDROID,
                permissionName = "com.custom.app.SPECIAL_PERMISSION",
                minSdkVersion = 28,
                isRequired = true
            ),
            PermissionMetadata(
                platform = TargetPlatform.IOS,
                permissionName = "NSCustomUsageDescription",
                usageDescriptionKey = "NSCustomUsageDescription",
                usageDescriptionDefault = "Requires custom access",
                isRequired = true
            )
        )

        val customPlugin = MockPlugin(
            id = PluginId.SYSTEM,
            requiredPermissions = emptyList(),
            permissionMetadata = customMetadata
        )

        val manifest = KSensor.permissionManifest(
            plugins = listOf(customPlugin),
            includeComments = false
        )
        assertTrue(manifest.contains("com.custom.app.SPECIAL_PERMISSION"))
        assertTrue(manifest.contains("android:minSdkVersion=\"28\""))

        val plist = KSensor.iosInfoPlistEntries(
            plugins = listOf(customPlugin),
            includeComments = false
        )
        assertTrue(plist.contains("<key>NSCustomUsageDescription</key>"))
        assertTrue(plist.contains("<string>Requires custom access</string>"))
    }
}
