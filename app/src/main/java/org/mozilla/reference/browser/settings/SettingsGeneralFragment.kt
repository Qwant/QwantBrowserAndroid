/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents

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

        // Links
        findPreference(context?.getPreferenceKey(R.string.pref_key_general_language)).onPreferenceClickListener = this.getPreferenceLinkListener(
                SettingsGeneralLanguageFragment(), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT"
        )
        // findPreference(context?.getPreferenceKey(R.string.pref_key_general_cleardata)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralClearDataFragment(this.settingsContainer), "SETTINGS_GENERAL_CLEARDATA_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_general_makedefaultbrowser)).onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()

        val prefNewsOnHome = findPreference(context?.getPreferenceKey(R.string.pref_key_general_newsonhome))
        prefNewsOnHome.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            requireComponents.core.sessionManager.sessions.forEach {
                if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                    var query: String? = null
                    if (it.url.contains("?q=") || it.url.contains("&q=")) {
                        query = it.url.split("?q=", "&q=")[1].split("&")[0]
                    }
                    val reloadPage = QwantUtils.getHomepage(requireContext(), query = query)
                    requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it.id)
                }
            }
            true
        }

        val prefResultInNewTabs = findPreference(context?.getPreferenceKey(R.string.pref_key_general_resultsinnewtab))
        prefResultInNewTabs.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            requireComponents.core.sessionManager.sessions.forEach {
                if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                    var query: String? = null
                    if (it.url.contains("?q=") || it.url.contains("&q=")) {
                        query = it.url.split("?q=", "&q=")[1].split("&")[0]
                    }
                    val reloadPage = QwantUtils.getHomepage(requireContext(), query = query, results_in_new_tab = value as Boolean)
                    requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it.id)
                }
            }
            true
        }

        val prefAdultContent = findPreference(context?.getPreferenceKey(R.string.pref_key_general_adultcontent)) as QwantPreferenceDropdown
        prefAdultContent.summary = adultContentValues[adultContentKeys.indexOf(prefAdultContent.value)]
        prefAdultContent.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            requireComponents.core.sessionManager.sessions.forEach {
                if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                    var query: String? = null
                    if (it.url.contains("?q=") || it.url.contains("&q=")) {
                        query = it.url.split("?q=", "&q=")[1].split("&")[0]
                    }
                    val reloadPage = QwantUtils.getHomepage(requireContext(), query = query, adult_content = value as String)
                    requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it.id)
                }
            }

            prefAdultContent.summary = adultContentValues[adultContentKeys.indexOf(value)]
            true
        }

        val prefTheme = findPreference(context?.getPreferenceKey(R.string.pref_key_general_dark_theme)) as QwantPreferenceDropdown
        prefTheme.summary = darkThemeValues[darkThemeKeys.indexOf(prefTheme.value)]
        prefTheme.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            requireComponents.core.sessionManager.sessions.forEach {
                if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                    var query: String? = null
                    if (it.url.contains("?q=") || it.url.contains("&q=")) {
                        query = it.url.split("?q=", "&q=")[1].split("&")[0]
                    }
                    val reloadPage = QwantUtils.getHomepage(requireContext(), query = query, dark_theme = value as String)
                    requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it.id)
                }
            }
            when (value) {
                "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            prefTheme.summary = darkThemeValues[darkThemeKeys.indexOf(value)]
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
        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsMainFragment(), "SETTINGS_MAIN_FRAGMENT")
                .addToBackStack(null)
                .commit()
        return true
    }
}
