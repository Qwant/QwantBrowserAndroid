package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R

abstract class BaseSettingsFragment: PreferenceFragmentCompat(), UserInteractionHandler {
    private var titleResourceId: Int = R.string.settings
    private var preferenceResourceId: Int = R.xml.preferences_general
    protected var settingsContainer: SettingsContainerFragment? = null

    fun setSettingsContainerFragment(settingsContainer: SettingsContainerFragment) {
        this.settingsContainer = settingsContainer
    }

    fun setup(titleResourceId: Int, preferenceResourceId: Int) {
        this.titleResourceId = titleResourceId
        this.preferenceResourceId = preferenceResourceId
    }

    abstract fun setupPreferences()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceResourceId, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsContainer?.setTitle(resources.getString(titleResourceId))
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsMainFragment(), "SETTINGS_MAIN_FRAGMENT")
                .addToBackStack(null)
                .commit()
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
