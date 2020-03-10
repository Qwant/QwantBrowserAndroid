/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.preference.Preference
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsGeneralFragment(
        private val settingsContainer: SettingsContainerFragment
) : BaseSettingsFragment(settingsContainer, R.string.settings_general, R.xml.preferences_general) {

    override fun setupPreferences() {
        // Links
        findPreference(context?.getPreferenceKey(R.string.pref_key_general_language)).onPreferenceClickListener = this.getPreferenceLinkListener(SettingsGeneralLanguageFragment(this.settingsContainer), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT")
        // findPreference(context?.getPreferenceKey(R.string.pref_key_general_adultcontent)).onPreferenceClickListener = this.getPreferenceLinkListener(SettingsGeneralAdultContentFragment(this.settingsContainer), "SETTINGS_GENERAL_ADULTCONTENT_FRAGMENT")
        // findPreference(context?.getPreferenceKey(R.string.pref_key_general_cleardata)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralClearDataFragment(this.settingsContainer), "SETTINGS_GENERAL_CLEARDATA_FRAGMENT")

        findPreference(context?.getPreferenceKey(R.string.pref_key_general_makedefaultbrowser)).onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()

        // TODO prefs to handle here
        // pref_key_general_newsonhome
        // pref_key_general_launchexternalapp
    }

    private fun getClickListenerForMakeDefaultBrowser(): Preference.OnPreferenceClickListener {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Preference.OnPreferenceClickListener {
                val intent = Intent(ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(intent)
                true
            }
        } else {
            Preference.OnPreferenceClickListener { preference ->
                Toast.makeText(context, "${preference.title} Clicked", LENGTH_SHORT).show()
                true
            }
        }
    }
}
