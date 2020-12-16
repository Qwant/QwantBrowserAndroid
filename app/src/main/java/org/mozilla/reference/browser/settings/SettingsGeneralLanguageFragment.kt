package org.mozilla.reference.browser.settings

import android.R.array
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents
import java.util.*


class SettingsGeneralLanguageFragment: BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.settings_general_language, R.xml.preferences_general_language)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

    override fun setupPreferences() {
        val interfaceKeys = resources.getStringArray(R.array.languages_interface_keys)
        val interfaceValues = resources.getStringArray(R.array.languages_interface_values)
        val searchKeys = resources.getStringArray(R.array.languages_search_keys)
        val searchValues = resources.getStringArray(R.array.languages_search_values)

        val prefLanguageInterface = findPreference(context?.getPreferenceKey(R.string.pref_key_general_language_interface)) as QwantPreferenceDropdown
        val prefLanguageSearch = findPreference(context?.getPreferenceKey(R.string.pref_key_general_language_search)) as QwantPreferenceDropdown
        val prefRegionSearch = findPreference(context?.getPreferenceKey(R.string.pref_key_general_region_search)) as QwantPreferenceDropdown

        prefLanguageInterface.summary = interfaceValues[interfaceKeys.indexOf(prefLanguageInterface.value)]
        val languageIndex = searchKeys.indexOf(prefLanguageSearch.value)
        prefLanguageSearch.summary = searchValues[languageIndex]
        toggleRegionForLanguage(languageIndex, prefRegionSearch.value)

        prefLanguageInterface.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            this.refreshQwantPage(interfaceLanguage = value as String)

            val localeStringSplit = value.split('_')
            val locale = Locale(localeStringSplit[0], localeStringSplit[1])
            Locale.setDefault(locale)
            resources.configuration.locale = locale
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)

            val intent = Intent(context, BrowserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.action = "CHANGED_LANGUAGE"
            startActivity(intent)

            true
        }
        prefLanguageSearch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            val index = searchKeys.indexOf(value)
            prefLanguageSearch.summary = searchValues[index]
            toggleRegionForLanguage(index)

            this.refreshQwantPage(searchLanguage = value as String)

            true
        }
        prefRegionSearch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            prefRegionSearch.summary = prefRegionSearch.entries[prefRegionSearch.findIndexOfValue(value as String)]

            this.refreshQwantPage(searchRegion = value)

            true
        }
    }

    override fun onBackPressed(): Boolean {
        fragmentManager?.beginTransaction()
                ?.replace(R.id.settings_fragment_container, SettingsGeneralFragment(), "SETTINGS_GENERAL_FRAGMENT")
                ?.addToBackStack(null)
                ?.commit()
        return true
    }

    private fun refreshQwantPage(interfaceLanguage: String? = null, searchLanguage: String? = null, searchRegion: String? = null) {
        requireComponents.core.sessionManager.sessions.forEach {
            if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                var query: String? = null
                if (it.url.contains("?q=") || it.url.contains("&q=")) {
                    query = it.url.split("?q=", "&q=")[1].split("&")[0]
                }
                val reloadPage = QwantUtils.getHomepage(requireContext(), query = query, interface_language = interfaceLanguage, search_language = searchLanguage, search_region = searchRegion)
                requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it.id)
            }
        }
    }

    private fun toggleRegionForLanguage(languageIndex: Int, value: String? = null) {
        val listArrayKeys = resources.obtainTypedArray(R.array.region_list_arrays_keys)
        val listArrayValues = resources.obtainTypedArray(R.array.region_list_arrays_values_sr)

        val arrayIdKeys = listArrayKeys.getResourceId(languageIndex, 0)
        val arrayIdValues = listArrayValues.getResourceId(languageIndex, 0)
        if (arrayIdKeys > 0 && arrayIdValues > 0) {
            val regionKeys = resources.getStringArray(arrayIdKeys)
            val regionValues = resources.getStringArray(arrayIdValues)

            val prefRegionSearch = findPreference(context?.getPreferenceKey(R.string.pref_key_general_region_search)) as QwantPreferenceDropdown
            prefRegionSearch.entries = regionKeys
            prefRegionSearch.entryValues = regionValues
            if (regionKeys.size > 1) {
                if (value != null) {
                    prefRegionSearch.value = value
                    val valueIndex = prefRegionSearch.findIndexOfValue(value)
                    if (valueIndex >= 0)
                        prefRegionSearch.summary = regionKeys[prefRegionSearch.findIndexOfValue(value)]
                    else
                        prefRegionSearch.summary = "probleme"
                }
                prefRegionSearch.isVisible = true
            } else {
                prefRegionSearch.value = regionValues[0]
                prefRegionSearch.summary = regionKeys[0]
                prefRegionSearch.isVisible = false
            }
            prefRegionSearch.forceNotifyChange()
        } else {
            Log.d("QWANT_BROWSER", "Error in language XML files")
        }

        listArrayKeys.recycle();
        listArrayValues.recycle();
    }
}
