/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_tabstray.*
// import kotlinx.android.synthetic.main.fragment_tabstray.tabsPanel
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.requireComponents

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment(
        val tabsClosedCallback: (() -> Unit)? = null,
        var isPrivate: Boolean = false
) : Fragment(), UserInteractionHandler {
    private var tabsFeature: TabsFeature? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_tabstray, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabsFeature = TabsFeature(
            tabsTray,
            requireComponents.core.sessionManager,
            requireComponents.useCases.tabsUseCases,
            ::closeTabsTray)

        tabsHeader.inflateMenu(R.menu.tabstray_menu)

        var color = context?.theme?.resolveAttribute(R.attr.qwant_color_main)
        if (color == null) color = R.color.qwant_main
        if (context != null) tabsHeader.setTitleTextColor(ContextCompat.getColor(context!!, color))

        /* tabsPanel.initialize(this.isPrivate, tabsFeature) { closeTabsTray() }
        tabsPanel.onTogglePrivacy { isPrivate: Boolean ->
            this.isPrivate = isPrivate
            context?.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
            activity?.supportFragmentManager?.beginTransaction()?.setReorderingAllowed(false)?.detach(this)?.attach(this)?.commit()
        } */
    }

    override fun onStart() {
        super.onStart()

        tabsFeature?.start()
    }

    override fun onStop() {
        super.onStop()

        tabsFeature?.stop()
    }

    override fun onBackPressed(): Boolean {
        closeTabsTray()
        return true
    }

    private fun closeTabsTray() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create(), "BROWSER_FRAGMENT")
            commit()
        }
        tabsClosedCallback?.invoke()
    }
}
