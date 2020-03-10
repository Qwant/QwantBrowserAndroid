package org.mozilla.reference.browser.settings

import androidx.preference.Preference
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsGeneralLanguageFragment(
        private val settingsContainer: SettingsContainerFragment
) : BaseSettingsFragment(settingsContainer, R.string.settings_general_language, R.xml.preferences_general_language) {
    override fun setupPreferences() {
        val interfaceKeys = resources.getStringArray(R.array.languages_interface_keys)
        val interfaceValues = resources.getStringArray(R.array.languages_interface_values)
        val searchKeys = resources.getStringArray(R.array.languages_interface_keys)
        val searchValues = resources.getStringArray(R.array.languages_interface_values)

        val prefLanguageInterface = findPreference(context?.getPreferenceKey(R.string.pref_key_general_language_interface)) as QwantPreferenceDropdown
        val prefLanguageSearch = findPreference(context?.getPreferenceKey(R.string.pref_key_general_language_search)) as QwantPreferenceDropdown

        prefLanguageInterface.summary = interfaceValues[interfaceKeys.indexOf(prefLanguageInterface.value)]
        prefLanguageSearch.summary = searchValues[searchKeys.indexOf(prefLanguageSearch.value)]

        prefLanguageInterface.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            prefLanguageInterface.summary = interfaceValues[interfaceKeys.indexOf(value)]
            true
        }
        prefLanguageSearch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            prefLanguageSearch.summary = searchValues.get(searchKeys.indexOf(value))
            true
        }
    }

    override fun onBackPressed(): Boolean {
        fragmentManager?.beginTransaction()
                ?.replace(R.id.settings_fragment_container, SettingsGeneralFragment(settingsContainer), "SETTINGS_GENERAL_FRAGMENT")
                ?.addToBackStack(null)
                ?.commit()
        return true
    }
}
