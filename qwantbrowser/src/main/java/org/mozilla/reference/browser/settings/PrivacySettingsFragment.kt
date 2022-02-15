/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceManager
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents

class PrivacySettingsFragment: BaseSettingsFragment(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.privacy_settings, R.xml.privacy_preferences)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

    override fun setupPreferences() {
        val trackingProtectionNormalKey = context?.getPreferenceKey(R.string.pref_key_tracking_protection_normal)
        val trackingProtectionPrivateKey = context?.getPreferenceKey(R.string.pref_key_tracking_protection_private)

        val prefTrackingProtectionNormal = findPreference(trackingProtectionNormalKey)
        val prefTrackingProtectionPrivate = findPreference(trackingProtectionPrivateKey)

        prefTrackingProtectionNormal.onPreferenceChangeListener = getChangeListenerForTrackingProtection { enabled ->
            requireComponents.core.createTrackingProtectionPolicy(normalMode = enabled)
        }
        prefTrackingProtectionPrivate.onPreferenceChangeListener = getChangeListenerForTrackingProtection { enabled ->
            requireComponents.core.createTrackingProtectionPolicy(privateMode = enabled)
        }

        val prefClearData = findPreference(context?.getPreferenceKey(R.string.pref_key_privacy_cleardata))
        val prefClearDataOnClose = findPreference(context?.getPreferenceKey(R.string.pref_key_privacy_cleardata_on_close_content))
        prefClearData.onPreferenceClickListener = getPreferenceLinkListener(ClearDataFragment(true), "SETTINGS_PRIVACY_CLEARALL_FRAGMENT")
        prefClearDataOnClose.onPreferenceClickListener = getPreferenceLinkListener(ClearDataFragment(false), "SETTINGS_PRIVACY_CLEARALL_FRAGMENT")
        prefClearDataOnClose.summary = getClearDataSummary()
    }

    private fun getClearDataSummary() : String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val enabled = sharedPrefs.getBoolean(getString(R.string.pref_key_privacy_cleardata_on_close), false)
        return if (enabled) {
            val allElements = arrayListOf<String>()

            val history = sharedPrefs.getBoolean(getString(R.string.pref_key_cleardata_content_history), false)
            if (history) allElements.add(getString(R.string.history))

            val tabs = sharedPrefs.getBoolean(getString(R.string.pref_key_cleardata_content_tabs), false)
            if (tabs) allElements.add(getString(R.string.cleardata_tabs))

            val privateTabs = sharedPrefs.getBoolean(getString(R.string.pref_key_cleardata_content_tabs_private), false)
            if (privateTabs) allElements.add(getString(R.string.cleardata_tabs_private))

            val browsingDataInt = sharedPrefs.getInt(getString(R.string.pref_key_cleardata_content_browsingdata), 0)
            val browsingData = Engine.BrowsingData.select(browsingDataInt)
            if (browsingData.contains(Engine.BrowsingData.ALL_SITE_DATA) && browsingData.contains(Engine.BrowsingData.AUTH_SESSIONS))
                allElements.add(getString(R.string.cleardata_allsites))
            else {
                if (browsingData.contains(Engine.BrowsingData.ALL_CACHES)) allElements.add(getString(R.string.cleardata_cache))
                if (browsingData.contains(Engine.BrowsingData.COOKIES)) allElements.add(getString(R.string.cleardata_cookies))
                if (browsingData.contains(Engine.BrowsingData.AUTH_SESSIONS)) allElements.add(getString(R.string.cleardata_sessions))
                if (browsingData.contains(Engine.BrowsingData.PERMISSIONS)) allElements.add(getString(R.string.cleardata_permissions))
            }

            allElements.joinToString()
        } else getString(R.string.disabled)
    }

    private fun getChangeListenerForTrackingProtection(
        createTrackingProtectionPolicy: (Boolean) -> TrackingProtectionPolicy
    ): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, value ->
            val policy = createTrackingProtectionPolicy(value as Boolean)
            requireComponents.useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
            true
        }
    }
}
