/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.*
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents

@Suppress("TooManyFunctions")
class SettingsFragment : PreferenceFragmentCompat() {

    interface ActionBarUpdater {
        fun updateTitle(titleResId: Int)
    }

    private val defaultClickListener = OnPreferenceClickListener { preference ->
        Toast.makeText(context, "${preference.title} Clicked", LENGTH_SHORT).show()
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        setupPreferences()
        getActionBarUpdater().apply {
            updateTitle(R.string.settings)
        }
    }

    @Suppress("LongMethod") // Yep, this should be refactored.
    private fun setupPreferences() {
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
            defaultClickListener
        }
    }

    private fun getClickListenerForPrivacy(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            fragmentManager?.beginTransaction()
                    ?.replace(android.R.id.content, PrivacySettingsFragment())
                    ?.addToBackStack(null)
                    ?.commit()
            getActionBarUpdater().apply {
                updateTitle(R.string.privacy_settings)
            }
            true
        }
    }

    private fun getAboutPageListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            fragmentManager?.beginTransaction()
                ?.replace(android.R.id.content, AboutFragment())
                ?.addToBackStack(null)
                ?.commit()
            true
        }
    }

    private fun getPrivacyPolicyListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            if (context != null) {
                context!!.components.useCases.tabsUseCases.addTab(context!!.getString(privacy_policy_url))
                activity?.finish()
            }
            true
        }
    }

    private fun getLicenseListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            if (context != null) {
                context!!.components.useCases.tabsUseCases.addTab(context!!.getString(license_url))
                activity?.finish()
            }
            true
        }
    }

    private fun getActionBarUpdater() = activity as ActionBarUpdater
}
