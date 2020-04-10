package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
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

        prefLanguageInterface.summary = interfaceValues[interfaceKeys.indexOf(prefLanguageInterface.value)]
        prefLanguageSearch.summary = searchValues[searchKeys.indexOf(prefLanguageSearch.value)]

        prefLanguageInterface.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            Log.d("QWANT_BROWSER","change interface language: $value")

            this.refreshQwantPage(interfaceLanguage = value as String)

            val localeStringSplit = (value as String).split('_')
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
            Log.d("QWANT_BROWSER","change search language: $value")

            this.refreshQwantPage(searchLanguage = value as String)

            prefLanguageSearch.summary = searchValues[searchKeys.indexOf(value)]
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

    private fun refreshQwantPage(interfaceLanguage: String? = null, searchLanguage: String? = null) {
        requireComponents.core.sessionManager.sessions.forEach {
            if (it.url.startsWith(requireContext().getString(R.string.homepage_startwith_filter))) {
                var query: String? = null
                if (it.url.contains("?q=") || it.url.contains("&q=")) {
                    query = it.url.split("?q=", "&q=")[1].split("&")[0]
                }
                val reloadPage = QwantUtils.getHomepage(requireContext(), query = query, interface_language = interfaceLanguage, search_language = searchLanguage)
                requireComponents.useCases.sessionUseCases.loadUrl(reloadPage, it)
            }
        }
    }
}
