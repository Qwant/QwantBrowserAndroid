/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsGeneralFragment: BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.settings_general, R.xml.preferences_general)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

    override fun setupPreferences() {
        val adultContentKeys = resources.getStringArray(R.array.adult_content_keys)
        val adultContentValues = resources.getStringArray(R.array.adult_content_values)

        val darkThemeKeys = resources.getStringArray(R.array.dark_theme_keys)
        val darkThemeValues = resources.getStringArray(R.array.dark_theme_values)

        findPreference<QwantPreference>(requireContext().getPreferenceKey(R.string.pref_key_general_language))?.onPreferenceClickListener = this.getPreferenceLinkListener(
                SettingsGeneralLanguageFragment(), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT"
        )
        findPreference<QwantPreference>(requireContext().getPreferenceKey(R.string.pref_key_general_makedefaultbrowser))?.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()

        val prefNewsOnHome = findPreference<QwantPreferenceSwitch>(requireContext().getPreferenceKey(R.string.pref_key_general_newsonhome))
        prefNewsOnHome?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            QwantUtils.refreshQwantPages(requireContext(), news_on_home = value as Boolean)
            true
        }

        val prefFaviconOnSerp = findPreference<QwantPreferenceSwitch>(requireContext().getPreferenceKey(R.string.pref_key_general_favicononserp))
        prefFaviconOnSerp?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            QwantUtils.refreshQwantPages(requireContext(), favicon_on_serp = value as Boolean)
            true
        }


        val prefResultInNewTabs = findPreference<QwantPreferenceSwitch>(requireContext().getPreferenceKey(R.string.pref_key_general_resultsinnewtab))
        prefResultInNewTabs?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            QwantUtils.refreshQwantPages(requireContext(), results_in_new_tab = value as Boolean)
            true
        }

        val prefAdultContent = findPreference<QwantPreferenceDropdown>(requireContext().getPreferenceKey(R.string.pref_key_general_adultcontent))
        prefAdultContent?.summary = adultContentValues[adultContentKeys.indexOf(prefAdultContent?.value)]
        prefAdultContent?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            prefAdultContent?.summary = adultContentValues[adultContentKeys.indexOf(value)]
            QwantUtils.refreshQwantPages(requireContext(), adult_content = value as String)
            true
        }

        val prefTheme = findPreference<QwantPreferenceDropdown>(requireContext().getPreferenceKey(R.string.pref_key_general_dark_theme))
        prefTheme?.summary = darkThemeValues[darkThemeKeys.indexOf(prefTheme?.value)]
        prefTheme?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            when (value) {
                "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            prefTheme?.summary = darkThemeValues[darkThemeKeys.indexOf(value)]
            // refreshFragment()

            QwantUtils.refreshQwantPages(requireContext(), dark_theme = value as String)
            true
        }
    }

    private fun refreshFragment() {
        // val fragmentManager = childFragmentManager
        // if (fragmentManager != null) {
            val currentFragment = childFragmentManager.findFragmentByTag("SETTINGS_GENERAL_FRAGMENT")
            val detachTransaction = childFragmentManager.beginTransaction()
            val attachTransaction = childFragmentManager.beginTransaction()

            currentFragment?.let {
                Log.d("QWANT_BROWSER", "fragment settings refreshed ok")
                detachTransaction.detach(it)
                attachTransaction.attach(it)
                detachTransaction.commit()
                attachTransaction.commit()
            }
        // }
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
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, SettingsMainFragment(), "SETTINGS_MAIN_FRAGMENT")
            .addToBackStack(null)
            .commit()
        return true
    }
}
