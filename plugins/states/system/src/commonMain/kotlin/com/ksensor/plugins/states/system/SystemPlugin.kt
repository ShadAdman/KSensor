package com.ksensor.plugins.states.system

import com.ksensor.core.KSensorPlugin
import com.ksensor.core.Permission
import com.ksensor.core.StatePlugin
import com.ksensor.core.model.PluginId
import com.ksensor.core.model.StateData
import com.ksensor.plugins.states.system.battery.BatteryStatusPlugin
import com.ksensor.plugins.states.system.battery.createBatteryStatusPlugin
import com.ksensor.plugins.states.system.brightness.BrightnessStatusPlugin
import com.ksensor.plugins.states.system.brightness.createBrightnessStatusPlugin
import com.ksensor.plugins.states.system.locale.LocaleStatusPlugin
import com.ksensor.plugins.states.system.locale.createLocaleStatusPlugin
import com.ksensor.plugins.states.system.lock.LockStatusPlugin
import com.ksensor.plugins.states.system.lock.createLockStatusPlugin
import com.ksensor.plugins.states.system.powersave.PowerSaveStatusPlugin
import com.ksensor.plugins.states.system.powersave.createPowerSaveStatusPlugin
import com.ksensor.plugins.states.system.resources.ResourcesStatusPlugin
import com.ksensor.plugins.states.system.resources.createResourcesStatusPlugin
import com.ksensor.plugins.states.system.screen.ScreenStatusPlugin
import com.ksensor.plugins.states.system.screen.createScreenStatusPlugin
import com.ksensor.plugins.states.system.storage.StorageStatusPlugin
import com.ksensor.plugins.states.system.storage.createStorageStatusPlugin
import com.ksensor.plugins.states.system.volume.VolumeStatusPlugin
import com.ksensor.plugins.states.system.volume.createVolumeStatusPlugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface SystemPlugin : KSensorPlugin {
    override val id: PluginId get() = PluginId.SYSTEM
    override val requiredPermissions: List<Permission> get() = emptyList()

    fun battery(): StatePlugin<StateData.BatteryStatus>
    fun volume(): StatePlugin<StateData.VolumeStatus>
    fun locale(): StatePlugin<StateData.LocaleStatus>
    fun screen(): StatePlugin<StateData.ScreenStatus>
    fun brightness(): StatePlugin<StateData.BrightnessStatus>
    fun lock(): StatePlugin<StateData.LockStatus>
    fun powerSave(): StatePlugin<StateData.PowerSaveStatus>
    fun storage(interval: Duration = 3.seconds): StatePlugin<StateData.StorageStatus>
    fun resources(interval: Duration = 3.seconds): StatePlugin<StateData.ResourcesStatus>
}

class DefaultSystemPlugin(
    private val batteryPlugin: BatteryStatusPlugin = createBatteryStatusPlugin(),
    private val volumePlugin: VolumeStatusPlugin = createVolumeStatusPlugin(),
    private val localePlugin: LocaleStatusPlugin = createLocaleStatusPlugin(),
    private val screenPlugin: ScreenStatusPlugin = createScreenStatusPlugin(),
    private val brightnessPlugin: BrightnessStatusPlugin = createBrightnessStatusPlugin(),
    private val lockPlugin: LockStatusPlugin = createLockStatusPlugin(),
    private val powerSavePlugin: PowerSaveStatusPlugin = createPowerSaveStatusPlugin(),
    private val storagePlugin: StorageStatusPlugin = createStorageStatusPlugin(),
    private val resourcesPlugin: ResourcesStatusPlugin = createResourcesStatusPlugin()
) : SystemPlugin {
    override fun battery(): StatePlugin<StateData.BatteryStatus> = batteryPlugin
    override fun volume(): StatePlugin<StateData.VolumeStatus> = volumePlugin
    override fun locale(): StatePlugin<StateData.LocaleStatus> = localePlugin
    override fun screen(): StatePlugin<StateData.ScreenStatus> = screenPlugin
    override fun brightness(): StatePlugin<StateData.BrightnessStatus> = brightnessPlugin
    override fun lock(): StatePlugin<StateData.LockStatus> = lockPlugin
    override fun powerSave(): StatePlugin<StateData.PowerSaveStatus> = powerSavePlugin
    override fun storage(interval: Duration): StatePlugin<StateData.StorageStatus> = storagePlugin.storage(interval)
    override fun resources(interval: Duration): StatePlugin<StateData.ResourcesStatus> = resourcesPlugin.resources(interval)
}

fun createSystemPlugin(): SystemPlugin = DefaultSystemPlugin()
