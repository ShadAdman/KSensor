package com.ksensor.plugins.states.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.ksensor.core.model.StateData

internal class LocaleReceiver(
    private val context: Context,
    private val onLocaleChanged: (StateData.LocaleStatus) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            onLocaleChanged(getCurrentLocale())
        }
    }

    fun getCurrentLocale(): StateData.LocaleStatus {
        val locale = context.resources.configuration.locales[0]
        val isRtl = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL

        return StateData.LocaleStatus(
            languageCode = locale.language,
            countryCode = locale.country,
            fullLocaleString = locale.toString(),
            displayName = locale.displayName,
            isRTL = isRtl
        )
    }
}
