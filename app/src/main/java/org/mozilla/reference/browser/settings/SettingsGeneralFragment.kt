/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

@Suppress("TooManyFunctions")
class SettingsGeneralFragment(
        private val settingsContainer: SettingsContainerFragment
) : BaseSettingsFragment(settingsContainer, R.string.settings_general, R.xml.preferences_general) {

    override fun setupPreferences() {
        // findPreference(context?.getPreferenceKey(R.string.pref_key_privacy)).onPreferenceClickListener = this.getPreferenceLinkListener(PrivacySettingsFragment(this.settingsContainer), "SETTINGS_PRIVACY_FRAGMENT")
        // findPreference(context?.getPreferenceKey(pref_key_general)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralFragment(), "SETTINGS_GENERAL_FRAGMENT")
        // findPreference(context?.getPreferenceKey(pref_key_interface)).onPreferenceClickListener = getPreferenceLinkListener(SettingsInterfaceFragment(), "SETTINGS_INTERFACE_FRAGMENT")
        // findPreference(context?.getPreferenceKey(pref_key_about)).onPreferenceClickListener = getPreferenceLinkListener(AboutFragment(), "SETTINGS_ABOUT_FRAGMENT")
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

    private fun getClickListenerForMakeDefaultBrowser(): OnPreferenceClickListener {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OnPreferenceClickListener {
                val intent = Intent(
                        Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                )
                startActivity(intent)
                true
            }
        } else {
            OnPreferenceClickListener { preference ->
                Toast.makeText(context, "${preference.title} Clicked", LENGTH_SHORT).show()
                true
            }
        }
    }

    private fun getClickListenerForPrivacy(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.settings_fragment_container, PrivacySettingsFragment(), "SETTINGS_PRIVACY_FRAGMENT")
                    .addToBackStack(null)
                    .commit()
            true
        }
    }

    private fun getAboutPageListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.settings_fragment_container, AboutFragment(), "SETTINGS_ABOUT_FRAGMENT")
                    .addToBackStack(null)
                    .commit()
            true
        }
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
