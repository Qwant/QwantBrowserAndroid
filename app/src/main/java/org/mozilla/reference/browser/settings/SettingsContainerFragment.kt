/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R

class SettingsContainerFragment: Fragment(), UserInteractionHandler {
    private var languageChangedReload: Boolean = false
    private var themeChangedReload: Boolean = false
    private var settingsClosedCallback: OnSettingsClosed? = null

    fun setOnSettingsClosed(callback: OnSettingsClosed) {
        this.settingsClosedCallback = callback
    }

    interface OnSettingsClosed {
        fun settingsClosed()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is BaseSettingsFragment) {
            childFragment.setSettingsContainerFragment(this)
        } else if (childFragment is ClearDataFragment) {
            childFragment.setSettingsContainerFragment(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // context?.theme?.applyStyle(R.style.ThemeQwantNoActionBar, true)
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tmp = arguments?.getBoolean(BUNDLE_LANGUAGE_CHANGE)
        if (tmp != null) languageChangedReload = tmp

        val tmp2 = arguments?.getBoolean(BUNDLE_THEME_CHANGE)
        if (tmp2 != null) themeChangedReload = tmp2

        settings_toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.qwant_text))
        settings_toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        when {
            languageChangedReload -> {
                childFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, SettingsGeneralLanguageFragment(), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
            }
            themeChangedReload -> {
                childFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, SettingsGeneralFragment(), "SETTINGS_GENERAL_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
            }
            else -> {
                childFragmentManager.beginTransaction()
                        .replace(R.id.settings_fragment_container, SettingsMainFragment(), "SETTINGS_MAIN_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        childFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return true
            }
        }

        closeSettings()
        return true
    }

    fun setTitle(title: String) {
        settings_toolbar.title = title
    }

    fun closeSettings() {
        (activity as BrowserActivity).showBrowserFragment()
        settingsClosedCallback?.settingsClosed()
    }

    companion object {
        private const val BUNDLE_LANGUAGE_CHANGE: String = "LANGUAGE_CHANGE"
        private const val BUNDLE_THEME_CHANGE: String = "THEME_CHANGE"

        fun create(language_changed_reload: Boolean = false, theme_changed_reload: Boolean = false) = SettingsContainerFragment().apply {
               arguments = Bundle().apply {
                   putBoolean(BUNDLE_LANGUAGE_CHANGE, language_changed_reload)
                   putBoolean(BUNDLE_THEME_CHANGE, theme_changed_reload)
               }
        }
    }
}
