package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.getPreferenceKey

class AboutMenuFragment: BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.settings_about_menu, R.xml.preferences_about)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

     override fun setupPreferences() {
        findPreference(context?.getPreferenceKey(R.string.pref_key_about)).onPreferenceClickListener = this.getPreferenceLinkListener(
            AboutFragment(), "SETTINGS_ABOUT_FRAGMENT"
        )
        findPreference(context?.getPreferenceKey(R.string.pref_key_privacy_policy)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (context != null) {
                requireContext().components.useCases.tabsUseCases.addTab(getString(R.string.privacy_policy_url), true)
                settingsContainer?.closeSettings()
            }
            true
        }
        findPreference(context?.getPreferenceKey(R.string.pref_key_license)).onPreferenceClickListener = this.getPreferenceLinkListener(
            LicenseFragment(), "SETTINGS_LICENSE_FRAGMENT"
        )
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsMainFragment(), "SETTINGS_MAIN_FRAGMENT")
                .addToBackStack(null)
                .commit()
        return true
    }
}