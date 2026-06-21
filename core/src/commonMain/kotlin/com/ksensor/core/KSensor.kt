package com.ksensor.core

/**
 * Main entry point for KSensor. Handles plugin registration and discovery.
 */
object KSensor {
    private val registry = mutableMapOf<PluginId, KSensorPlugin>()

    /**
     * Registers a plugin.
     */
    fun register(plugin: KSensorPlugin) {
        registry[plugin.id] = plugin
    }

    /**
     * Unregisters a plugin.
     */
    fun unregister(id: PluginId) {
        registry.remove(id)
    }

    /**
     * Retrieves a registered plugin by its ID.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : KSensorPlugin> get(id: PluginId): T? {
        return registry[id] as? T
    }

    /**
     * Checks if a plugin is registered.
     */
    fun hasPlugin(id: PluginId): Boolean = registry.containsKey(id)
}
