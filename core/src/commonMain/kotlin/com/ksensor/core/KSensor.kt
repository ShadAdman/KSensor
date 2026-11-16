package com.ksensor.core

import com.ksensor.core.benchmark.BenchmarkMonitor
import com.ksensor.core.model.PluginId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Main entry point for KSensor. Handles plugin registration, discovery, and lifecycle diagnostics.
 */
object KSensor {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val registry = mutableMapOf<PluginId, KSensorPlugin>()
    private val storage: PlatformStorage by lazy { createPlatformStorage() }

    private val _diagnosticsMap = MutableStateFlow<Map<PluginId, PluginDiagnostics>>(emptyMap())

    /**
     * Live [StateFlow] emitting updated health and status diagnostics for all registered plugins.
     */
    val diagnosticsFlow: StateFlow<Map<PluginId, PluginDiagnostics>> = _diagnosticsMap.asStateFlow()

    private val diagnosticsObservers = mutableSetOf<PluginDiagnosticsObserver>()

    private val KEY_START_ON_BOOT = "ksensor_start_on_boot"
    private val KEY_BOOT_PLUGINS = "ksensor_boot_plugins"

    /**
     * Flag to enable/disable "start on boot" for the entire library.
     */
    var startOnBoot: Boolean
        get() = storage.getBoolean(KEY_START_ON_BOOT, false)
        set(value) = storage.putBoolean(KEY_START_ON_BOOT, value)

    private val _startOnBootPlugins: MutableSet<PluginId> by lazy {
        storage.getStringSet(KEY_BOOT_PLUGINS, emptySet())
            .map { PluginId.valueOf(it) }
            .toMutableSet()
    }

    private fun persistBootPlugins() {
        storage.putStringSet(KEY_BOOT_PLUGINS, _startOnBootPlugins.map { it.name }.toSet())
    }

    /**
     * Permission handler for checking and requesting permissions.
     */
    val permissionHandler: PermissionHandler by lazy { createPermissionHandler() }

    /**
     * Live telemetry collector and benchmark monitor for KSensor.
     */
    val benchmark: BenchmarkMonitor get() = BenchmarkMonitor

    /**
     * Registers a plugin and computes its initial health status diagnostics.
     * @param startOnBoot If true, this plugin will be marked to start automatically on device boot.
     */
    fun register(plugin: KSensorPlugin, startOnBoot: Boolean = false) {
        if (registry.containsKey(plugin.id)) return

        registry[plugin.id] = plugin
        if (startOnBoot) {
            _startOnBootPlugins.add(plugin.id)
            persistBootPlugins()
        }

        refreshDiagnostics(plugin.id)
    }

    /**
     * Unregisters a plugin and removes its entry from diagnostics.
     */
    fun unregister(id: PluginId) {
        if (registry.containsKey(id).not()) throw Exception("Plugin with id $id not found")

        registry.remove(id)
        _diagnosticsMap.value -= id
    }

    /**
     * Retrieves a registered plugin by its ID.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : KSensorPlugin> get(id: PluginId): T? {
        return registry[id] as? T
    }

    /**
     * Retrieves all currently registered plugins.
     */
    fun getRegisteredPlugins(): List<KSensorPlugin> = registry.values.toList()

    /**
     * Retrieves current diagnostics for all registered plugins.
     */
    fun diagnostics(): Map<PluginId, PluginDiagnostics> = _diagnosticsMap.value

    /**
     * Retrieves current diagnostics for a specific plugin ID.
     */
    fun diagnostics(id: PluginId): PluginDiagnostics? = _diagnosticsMap.value[id]

    /**
     * Returns a list of plugin diagnostics matching the given [PluginStatus].
     */
    fun filterStatus(status: PluginStatus): List<PluginDiagnostics> {
        return _diagnosticsMap.value.values.filter { it.status == status }
    }

    /**
     * Registers a global diagnostics observer/sink for receiving plugin status updates.
     */
    fun addDiagnosticsObserver(observer: PluginDiagnosticsObserver) {
        diagnosticsObservers.add(observer)
    }

    /**
     * Removes a previously registered global diagnostics observer/sink.
     */
    fun removeDiagnosticsObserver(observer: PluginDiagnosticsObserver) {
        diagnosticsObservers.remove(observer)
    }

    /**
     * Reports a status change for a plugin and notifies all diagnostics sinks.
     */
    fun reportStatus(
        pluginId: PluginId,
        status: PluginStatus,
        errorMessage: String? = null,
        unsupportedReason: String? = null
    ) {
        val plugin = registry[pluginId]
        val existing = _diagnosticsMap.value[pluginId]

        val platformAvailable = plugin?.isSupported ?: existing?.isPlatformAvailable ?: true
        val permissionState = if (plugin != null) {
            if (plugin.requiredPermissions.isEmpty()) PermissionStatus.GRANTED
            else if (plugin.requiredPermissions.all { permissionHandler.hasPermission(it) }) PermissionStatus.GRANTED
            else PermissionStatus.DENIED
        } else existing?.permissionState ?: PermissionStatus.UNKNOWN

        val updatedReason = unsupportedReason ?: plugin?.unsupportedReason ?: existing?.unsupportedReason

        val finalStatus = when {
            !platformAvailable -> PluginStatus.UNSUPPORTED
            permissionState == PermissionStatus.DENIED && status != PluginStatus.ERROR -> PluginStatus.BLOCKED_BY_PERMISSION
            else -> status
        }

        val newDiag = DefaultPluginDiagnostics(
            pluginId = pluginId,
            status = finalStatus,
            lastUpdatedTimestamp = currentTimestamp(),
            lastErrorMessage = if (finalStatus == PluginStatus.ERROR) (errorMessage ?: existing?.lastErrorMessage) else errorMessage,
            unsupportedReason = updatedReason,
            permissionState = permissionState,
            isPlatformAvailable = platformAvailable
        )

        _diagnosticsMap.value += (pluginId to newDiag)
        diagnosticsObservers.toList().forEach { observer ->
            try {
                observer.onDiagnosticsUpdated(newDiag)
            } catch (e: Exception) {
                // Ignore observer handler exceptions
            }
        }
    }

    /**
     * Re-evaluates permission state, platform availability, and status for a specific plugin or all registered plugins.
     */
    fun refreshDiagnostics(id: PluginId? = null) {
        val targetIds = if (id != null) listOfNotNull(registry[id]?.id) else registry.keys.toList()

        targetIds.forEach { targetId ->
            val plugin = registry[targetId] ?: return@forEach
            val existing = _diagnosticsMap.value[targetId]

            val platformAvailable = plugin.isSupported
            val permissionState = if (plugin.requiredPermissions.isEmpty()) {
                PermissionStatus.GRANTED
            } else {
                val allGranted = plugin.requiredPermissions.all { permissionHandler.hasPermission(it) }
                if (allGranted) PermissionStatus.GRANTED else PermissionStatus.DENIED
            }

            val computedStatus = when {
                !platformAvailable -> PluginStatus.UNSUPPORTED
                permissionState == PermissionStatus.DENIED -> PluginStatus.BLOCKED_BY_PERMISSION
                existing?.status == PluginStatus.ACTIVE -> PluginStatus.ACTIVE
                existing?.status == PluginStatus.ERROR -> PluginStatus.ERROR
                else -> PluginStatus.READY
            }

            val newDiag = DefaultPluginDiagnostics(
                pluginId = targetId,
                status = computedStatus,
                lastUpdatedTimestamp = currentTimestamp(),
                lastErrorMessage = existing?.lastErrorMessage,
                unsupportedReason = plugin.unsupportedReason ?: existing?.unsupportedReason,
                permissionState = permissionState,
                isPlatformAvailable = platformAvailable
            )

            _diagnosticsMap.value = _diagnosticsMap.value + (targetId to newDiag)
            diagnosticsObservers.toList().forEach { observer ->
                try {
                    observer.onDiagnosticsUpdated(newDiag)
                } catch (e: Exception) {
                    // Ignore observer exceptions
                }
            }
        }
    }

    /**
     * Generates AndroidManifest.xml permission snippet for the specified or registered plugins.
     */
    fun permissionManifest(
        plugins: List<KSensorPlugin> = getRegisteredPlugins(),
        includeOptional: Boolean = true,
        includeComments: Boolean = true
    ): String = PermissionManifestGenerator.generateAndroidManifest(
        metadataList = plugins.flatMap { it.permissionMetadata },
        includeOptional = includeOptional,
        includeComments = includeComments
    )

    /**
     * Generates iOS Info.plist permission entries snippet for the specified or registered plugins.
     */
    fun iosInfoPlistEntries(
        plugins: List<KSensorPlugin> = getRegisteredPlugins(),
        includeOptional: Boolean = true,
        includeComments: Boolean = true
    ): String = PermissionManifestGenerator.generateIosInfoPlist(
        metadataList = plugins.flatMap { it.permissionMetadata },
        includeOptional = includeOptional,
        includeComments = includeComments
    )

    /**
     * Debug utility that prints the exact AndroidManifest.xml and iOS Info.plist entries for registered plugins.
     */
    fun printPermissionManifests(
        plugins: List<KSensorPlugin> = getRegisteredPlugins()
    ) {
        println("=== AndroidManifest.xml Permissions ===")
        println(permissionManifest(plugins))
        println("\n=== iOS Info.plist Permissions ===")
        println(iosInfoPlistEntries(plugins))
    }

    /**
     * Checks if a plugin is registered.
     */
    fun hasPlugin(id: PluginId): Boolean = registry.containsKey(id)

    /**
     * Returns the list of plugin IDs that are marked to start on boot.
     */
    fun getStartOnBootPlugins(): Set<PluginId> = _startOnBootPlugins.toSet()

    /**
     * Marks a plugin to start on boot or removes the mark.
     */
    fun setStartOnBoot(id: PluginId, enable: Boolean) {
        if (enable) {
            _startOnBootPlugins.add(id)
        } else {
            _startOnBootPlugins.remove(id)
        }
        persistBootPlugins()
    }

    /**
     * Starts all plugins marked as "start on boot".
     */
    fun start() {
        if (!startOnBoot) return

        _startOnBootPlugins.forEach { id ->
            val plugin = registry[id]
            if (plugin != null) {
                scope.launch {
                    when (plugin) {
                        is SensorPlugin<*> -> {
                            plugin.observe().collect { }
                        }
                        is StatePlugin<*> -> {
                            plugin.observe().collect { }
                        }
                    }
                }
            }
        }
    }

    internal fun onBoot() {
        start()
    }
}

/**
 * Extension on [Flow] to automatically track lifecycle status changes (ACTIVE, ERROR, READY) in KSensor diagnostics.
 */
fun <T> Flow<T>.trackPluginDiagnostics(pluginId: PluginId): Flow<T> {
    return this
        .onStart {
            KSensor.reportStatus(pluginId, PluginStatus.ACTIVE)
        }
        .catch { e ->
            KSensor.reportStatus(
                pluginId = pluginId,
                status = PluginStatus.ERROR,
                errorMessage = e.message ?: "Unknown error in plugin flow"
            )
            throw e
        }
        .onCompletion { cause ->
            if (cause == null) {
                val current = KSensor.diagnostics(pluginId)
                if (current?.status == PluginStatus.ACTIVE) {
                    KSensor.reportStatus(pluginId, PluginStatus.READY)
                }
            }
        }
}
