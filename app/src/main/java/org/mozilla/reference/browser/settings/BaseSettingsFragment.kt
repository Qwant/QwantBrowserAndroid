package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R

abstract class BaseSettingsFragment(
        private val settingsContainer: SettingsContainerFragment,
        private val titleResourceId: Int,
        private val preferenceResourceId: Int
) : PreferenceFragmentCompat(), UserInteractionHandler {
    abstract fun setupPreferences()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceResourceId, rootKey)
        settingsContainer.setTitle(resources.getString(titleResourceId))
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
    }

    override fun onBackPressed(): Boolean {
        fragmentManager?.beginTransaction()
            ?.replace(R.id.settings_fragment_container, SettingsMainFragment(settingsContainer), "SETTINGS_MAIN_FRAGMENT")
            ?.addToBackStack(null)
            ?.commit()
        return true
    }

    fun getPreferenceLinkListener(target: Fragment, fragment_id: String): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            if (context != null) {
                parentFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, target, fragment_id)
                        .addToBackStack(null)
                        .commit()
            }
            true
        }
    }
}
