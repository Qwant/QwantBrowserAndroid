/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference.OnPreferenceChangeListener
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
