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
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment


class SettingsContainerFragment(
        private val settingsClosedCallback: (() -> Unit)? = null,
        private val language_changed_reload: Boolean = false
) : Fragment(), UserInteractionHandler {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        context?.theme?.applyStyle(R.style.ThemeQwantNoActionBar, true);
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings_toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        if (language_changed_reload) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.settings_fragment_container, SettingsGeneralLanguageFragment(this), "SETTINGS_GENERAL_LANGUAGE_FRAGMENT")
                    .addToBackStack(null)
                    .commit()
        } else {
            childFragmentManager.beginTransaction()
                    .replace(R.id.settings_fragment_container, SettingsMainFragment(this), "SETTINGS_MAIN_FRAGMENT")
                    .addToBackStack(null)
                    .commit()
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
        parentFragmentManager.beginTransaction()
                .replace(R.id.container, BrowserFragment.create(), "BROWSER_FRAGMENT")
                .commit()
        settingsClosedCallback?.invoke()
    }
}
