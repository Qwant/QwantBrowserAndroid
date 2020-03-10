package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class AboutMenuFragment(
        private val settingsContainer: SettingsContainerFragment
) : BaseSettingsFragment(settingsContainer, R.string.settings_about_menu, R.xml.preferences_about) {
     override fun setupPreferences() {
        findPreference(context?.getPreferenceKey(R.string.pref_key_about)).onPreferenceClickListener = this.getPreferenceLinkListener(
            AboutFragment(this.settingsContainer), "SETTINGS_ABOUT_FRAGMENT"
        )
        findPreference(context?.getPreferenceKey(R.string.pref_key_privacy_policy)).onPreferenceClickListener = this.getPreferenceLinkListener(
            PrivacyPolicyFragment(this.settingsContainer), "SETTINGS_PRIVACYPOLICY_FRAGMENT"
        )
        findPreference(context?.getPreferenceKey(R.string.pref_key_license)).onPreferenceClickListener = this.getPreferenceLinkListener(
            LicenseFragment(this.settingsContainer), "SETTINGS_LICENSE_FRAGMENT"
        )
    }

    override fun onBackPressed(): Boolean {
        fragmentManager?.beginTransaction()
                ?.replace(R.id.settings_fragment_container, SettingsMainFragment(settingsContainer), "SETTINGS_MAIN_FRAGMENT")
                ?.addToBackStack(null)
                ?.commit()
        return true
    }
}