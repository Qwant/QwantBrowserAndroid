/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.preference.Preference
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsGeneralFragment(
        private val settingsContainer: SettingsContainerFragment
) : BaseSettingsFragment(settingsContainer, R.string.settings_general, R.xml.preferences_general) {

    override fun setupPreferences() {
        val adultContentKeys = resources.getStringArray(R.array.adult_content_keys)
        val adultContentValues = resources.getStringArray(R.array.adult_content_values)

        // Links
        findPreference(context?.getPreferenceKey(R.string.pref_key_general_language)).onPreferenceClickListener = this.getPreferenceLinkListener(
                SettingsGeneralLanguageFragment(this.settingsContainer), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT"
        )
        // findPreference(context?.getPreferenceKey(R.string.pref_key_general_cleardata)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralClearDataFragment(this.settingsContainer), "SETTINGS_GENERAL_CLEARDATA_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_general_makedefaultbrowser)).onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()

        val prefAdultContent = findPreference(context?.getPreferenceKey(R.string.pref_key_general_adultcontent)) as QwantPreferenceDropdown

        Log.d("QWANT_BROWSER", "adult content value: ${prefAdultContent.value}")
        adultContentKeys.forEach { s -> Log.d("QWANT_BROWSER", "adult content keys: $s") }

        prefAdultContent.summary = adultContentValues[adultContentKeys.indexOf(prefAdultContent.value)]
        prefAdultContent.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            prefAdultContent.summary = adultContentValues[adultContentKeys.indexOf(value)]
            true
        }
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

    override fun onBackPressed(): Boolean {
        fragmentManager?.beginTransaction()
                ?.replace(R.id.settings_fragment_container, SettingsMainFragment(settingsContainer), "SETTINGS_MAIN_FRAGMENT")
                ?.addToBackStack(null)
                ?.commit()
        return true
    }
}
