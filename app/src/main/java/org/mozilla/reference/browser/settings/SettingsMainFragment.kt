/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.fragment.app.Fragment
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsMainFragment(
        val settingsContainer: SettingsContainerFragment
) : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
        settingsContainer.setTitle("Settings")
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
    }

    private fun setupPreferences() {
        findPreference(context?.getPreferenceKey(R.string.pref_key_privacy)).onPreferenceClickListener = getPreferenceLinkListener(PrivacySettingsFragment(settingsContainer), "SETTINGS_PRIVACY_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_general)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralFragment(settingsContainer), "SETTINGS_GENERAL_FRAGMENT")
        // findPreference(context?.getPreferenceKey(pref_key_interface)).onPreferenceClickListener = getPreferenceLinkListener(SettingsInterfaceFragment(), "SETTINGS_INTERFACE_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_about)).onPreferenceClickListener = getPreferenceLinkListener(AboutFragment(settingsContainer), "SETTINGS_ABOUT_FRAGMENT")
    }

    private fun getPreferenceLinkListener(target: Fragment, fragment_id: String): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            if (context != null) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.settings_fragment_container, target, fragment_id)
                    .addToBackStack(null)
                    .commit()
            }
            true
        }
    }

    /* private fun setupPreferences() {
        val makeDefaultBrowserKey = context?.getPreferenceKey(pref_key_make_default_browser)
        val aboutPageKey = context?.getPreferenceKey(pref_key_about_page)
        val privacyKey = context?.getPreferenceKey(pref_key_privacy)
        val privacyPolicyKey = context?.getPreferenceKey(pref_key_privacy_policy)
        val licenseKey = context?.getPreferenceKey(pref_key_license)

        val preferenceMakeDefaultBrowser = findPreference(makeDefaultBrowserKey)
        val preferenceAboutPage = findPreference(aboutPageKey)
        val preferencePrivacy = findPreference(privacyKey)

        val preferencePrivacyPolicy = findPreference(privacyPolicyKey)
        val preferenceLicense = findPreference(licenseKey)

        preferenceMakeDefaultBrowser.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()
        preferenceAboutPage.onPreferenceClickListener = getAboutPageListener()
        preferencePrivacy.onPreferenceClickListener = getClickListenerForPrivacy()

        preferencePrivacyPolicy.onPreferenceClickListener = getPrivacyPolicyListener()
        preferenceLicense.onPreferenceClickListener = getLicenseListener()
    }

    private fun getPrivacyPolicyListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            if (context != null) {
                context!!.components.useCases.tabsUseCases.addTab(context!!.getString(privacy_policy_url))
                parentFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, BrowserFragment.create(), "BROWSER_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
            }
            true
        }
    }

    private fun getLicenseListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            if (context != null) {
                context!!.components.useCases.tabsUseCases.addTab(context!!.getString(license_url))
                parentFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, BrowserFragment.create(), "BROWSER_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
            }
            true
        }
    } */
}
