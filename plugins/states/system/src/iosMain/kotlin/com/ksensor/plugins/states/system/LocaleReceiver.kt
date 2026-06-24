package com.ksensor.plugins.states.system

import com.ksensor.core.model.StateData
import platform.Foundation.*
import platform.darwin.NSObject

internal class LocaleReceiver(private val onLocaleChanged: (StateData.LocaleStatus) -> Unit) {
    private var observer: NSObject? = null

    fun register() {
        observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = NSCurrentLocaleDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue()
        ) { _ ->
            onLocaleChanged(getCurrentLocale())
        } as NSObject?
    }

    fun unregister() {
        observer?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
            observer = null
        }
    }

    fun getCurrentLocale(): StateData.LocaleStatus {
        val locale = NSLocale.currentLocale
        val languageCode = locale.languageCode

        val direction = NSLocale.characterDirectionForLanguage(languageCode)
        val isRtl = direction == NSLocaleLanguageDirectionRightToLeft

        return StateData.LocaleStatus(
            languageCode = languageCode,
            countryCode = locale.countryCode ?: "",
            fullLocaleString = locale.localeIdentifier,
            displayName = locale.displayNameForKey(NSLocaleIdentifier, locale.localeIdentifier) ?: locale.localeIdentifier,
            isRTL = isRtl
        )
    }
}
