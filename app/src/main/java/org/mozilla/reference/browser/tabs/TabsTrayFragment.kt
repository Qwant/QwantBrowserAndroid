/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tabstray.*
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.tabs.tray.BrowserTabsTray
import org.mozilla.reference.browser.tabs.tray.TabsFeature
import java.lang.ref.WeakReference

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment(
        val applicationContext: Context,
        val tabsClosedCallback: (() -> Unit)? = null,
        var isPrivate: Boolean = false,
        var qwantbar: QwantBar
) : Fragment(), UserInteractionHandler {
    private var tabsFeature: TabsFeature? = null

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private val sessionManager: SessionManager = applicationContext.application.components.core.sessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_tabstray, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reference = WeakReference(tab_switch_normal_counter)

        val tray = tabsTray
        if (tray is BrowserTabsTray) {
            tray.onTabsChangedRegister {
                this.updateTabCount()
            }
        }

        tabsFeature = TabsFeature(
            tabsTray,
            requireComponents.core.store,
            requireComponents.useCases.tabsUseCases,
            closeTabsTray = ::closeTabsTray)


        tabsFeature?.filterTabs { it.content.private == isPrivate }

        val context = requireContext()
        if (isPrivate) {
            button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mozac_ic_private_browsing, 0, 0, 0)
            tab_switch_normal_counter.background = null
            tab_switch_normal_counter.elevation = 0F
            tab_switch_private_browsing_icon.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            tab_switch_private_browsing_icon.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
        } else {
            button_new_tab.setTextColor(ContextCompat.getColor(context, R.color.qwant_selected_text))
        }

        // tabsHeader.inflateMenu(R.menu.tabstray_menu)

        tab_switch_button_background.setOnClickListener((View.OnClickListener {
            this.isPrivate = !isPrivate
            context.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
            qwantbar.setPrivacyMode(isPrivate)
            activity?.supportFragmentManager?.beginTransaction()?.setReorderingAllowed(false)?.detach(this)?.attach(this)?.commit()
        }))

        button_new_tab.setOnClickListener((View.OnClickListener {
            if (isPrivate) {
                context.components.useCases.tabsUseCases.addPrivateTab.invoke(context.getString(R.string.homepage), selectTab = true)
            } else {
                context.components.useCases.tabsUseCases.addTab.invoke(context.getString(R.string.homepage), selectTab = true)
            }
            this.closeTabsTray()
        }))

        this.updateTabCount()
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

    private fun closeTabsTray(): Unit {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create(), "BROWSER_FRAGMENT")
            commit()
        }
        tabsClosedCallback?.invoke()
    }

    private fun updateTabCount() {
        reference.get()?.setCountWithAnimation(sessionManager.sessions.filter { it.private == isPrivate }.size)
    }
}
