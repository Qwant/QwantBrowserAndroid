/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey

class SettingsMainFragment: BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.setup(R.string.settings_main, R.xml.preferences_main)
        super.onCreatePreferences(savedInstanceState, rootKey)
    }

    override fun setupPreferences() {
        findPreference(context?.getPreferenceKey(R.string.pref_key_privacy)).onPreferenceClickListener = getPreferenceLinkListener(PrivacySettingsFragment(), "SETTINGS_PRIVACY_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_general)).onPreferenceClickListener = getPreferenceLinkListener(SettingsGeneralFragment(), "SETTINGS_GENERAL_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_about_menu)).onPreferenceClickListener = getPreferenceLinkListener(AboutMenuFragment(), "SETTINGS_ABOUTMENU_FRAGMENT")
        findPreference(context?.getPreferenceKey(R.string.pref_key_history)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (activity as BrowserActivity).showHistory()
            true
        }
        findPreference(context?.getPreferenceKey(R.string.pref_key_rate_app)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.qwant.liberty"))
            context?.startActivity(intent)
            true
        }

        val quitAppPref = findPreference(context?.getPreferenceKey(R.string.pref_key_quit_app))
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getString(R.string.pref_key_privacy_cleardata_on_close) , false)) {
            quitAppPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                (activity as BrowserActivity).quitApp()
                true
            }
        } else {
            quitAppPref.isVisible = false
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
