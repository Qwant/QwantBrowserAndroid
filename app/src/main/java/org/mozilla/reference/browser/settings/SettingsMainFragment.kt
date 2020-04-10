/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsMainFragment: BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.settings_main, R.xml.preferences_main)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

    override fun setupPreferences() {
        findPreference(context?.getPreferenceKey(R.string.pref_key_privacy)).onPreferenceClickListener = getPreferenceLinkListener(PrivacySettingsFragment(), "SETTINGS_PRIVACY_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_general)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralFragment(), "SETTINGS_GENERAL_FRAGMENT")
        // findPreference(context?.getPreferenceKey(pref_key_interface)).onPreferenceClickListener = getPreferenceLinkListener(SettingsInterfaceFragment(), "SETTINGS_INTERFACE_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_about_menu)).onPreferenceClickListener = getPreferenceLinkListener(AboutMenuFragment(), "SETTINGS_ABOUTMENU_FRAGMENT")
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
