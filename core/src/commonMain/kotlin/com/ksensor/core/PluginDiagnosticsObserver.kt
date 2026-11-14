package com.ksensor.core

/**
 * Observer or sink interface for receiving global diagnostics updates across all registered plugins.
 */
fun interface PluginDiagnosticsObserver {
    fun onDiagnosticsUpdated(diagnostics: PluginDiagnostics)
}
