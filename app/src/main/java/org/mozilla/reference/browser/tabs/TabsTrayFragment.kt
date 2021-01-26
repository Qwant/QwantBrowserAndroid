/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_tabstray.*
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.ext.toCustomTabSessionState
import mozilla.components.browser.session.ext.toTabSessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.settings.SettingsContainerFragment
import org.mozilla.reference.browser.tabs.tray.BrowserTabsTray
import org.mozilla.reference.browser.tabs.tray.TabsFeature
import java.lang.ref.WeakReference

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment: Fragment(), UserInteractionHandler {
    private var tabsClosedCallback: (() -> Unit)? = null
    fun setTabsClosedCallback(tabsClosedCallback: (() -> Unit)) { this.tabsClosedCallback = tabsClosedCallback }
    private var isPrivate: Boolean = false
    fun setPrivacy(is_private: Boolean) { this.isPrivate = is_private }
    private var qwantbar: QwantBar? = null
    fun setQwantBar(bar: QwantBar) { this.qwantbar = bar }


    private var tabsFeature: TabsFeature? = null

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private var sessionManager: SessionManager? = null
    private var applicationContext: Context? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (activity != null) {
            applicationContext = requireActivity().applicationContext
            sessionManager = requireActivity().application.components.core.sessionManager
        }
        super.onActivityCreated(savedInstanceState)
    }

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

        /* tabsFeature = TabsFeature(
            tabsTray,
            requireComponents.core.store,
            requireComponents.useCases.tabsUseCases,
            closeTabsTray = ::closeTabsTray) */


        tabsFeature = TabsFeature(
                tabsTray,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases,
                // requireComponents.useCases.thumbnailUseCases,
                { it.content.private == isPrivate },
                ::closeTabsTray)

        // tabsFeature?.filterTabs { it.content.private == isPrivate }

        val context = requireContext()

        if (isPrivate) {
            button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_privacy_mask_white, 0, 0, 0)
            tab_switch_normal_counter.background = null
            tab_switch_normal_counter.elevation = 0F

            val tabCounterBox: ImageView = tab_switch_normal_counter.findViewById(R.id.counter_box)
            // val tabCounterBar: ImageView  = tab_switch_normal_counter.findViewById(R.id.counter_bar)
            val tabCounterText: TextView = tab_switch_normal_counter.findViewById(R.id.counter_text)
            val tabColor = ContextCompat.getColor(context, R.color.photonWhite)
            tabCounterBox.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, tabColor))
            // tabCounterBar.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_bar, tabColor))
            tabCounterText.setTextColor(tabColor)

            tab_switch_private_browsing_icon.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            tab_switch_private_browsing_icon.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
        } else {
            button_new_tab.setTextColor(ContextCompat.getColor(context, R.color.qwant_selected_text))
        }

        // tabsHeader.inflateMenu(R.menu.tabstray_menu)

        tab_menu_more.setColorFilter(ContextCompat.getColor(context, R.color.menu_items))
        tab_menu_more.menuBuilder = BrowserMenuBuilder(listOf(
            BrowserMenuImageText(
                context.getString(R.string.menu_action_close_tabs),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.icon_cross
            ) {
                context.components.useCases.tabsUseCases.removeAllTabs.invoke()
            }
        ))

        back_tabs_button.setOnClickListener((View.OnClickListener {
            this.onBackPressed()
        }))

        tab_switch_button_background.setOnClickListener((View.OnClickListener {
            this.isPrivate = !isPrivate
            context.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
            qwantbar?.setPrivacyMode(isPrivate)
            activity?.supportFragmentManager?.beginTransaction()?.setReorderingAllowed(false)?.detach(this)?.attach(this)?.commit()
        }))

        button_new_tab.setOnClickListener((View.OnClickListener {
            if (applicationContext != null) {
                Log.d("QWANT_BROWSER", "new tab ua top: ${context.components.core.engine.settings.userAgentString} ")
                if (isPrivate) {
                    context.components.useCases.tabsUseCases.addPrivateTab.invoke(QwantUtils.getHomepage(applicationContext!!))
                    // context.components.useCases.tabsUseCases.addPrivateTab.invoke(QwantUtils.getHomepage(applicationContext!!), true, parentId = context.components.core.sessionManager.selectedSession?.id)
                } else {
                    context.components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(applicationContext!!))
                }
                this.closeTabsTray()
            }
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
        isPrivate = (sessionManager != null && sessionManager!!.selectedSession != null && sessionManager!!.selectedSession!!.private)
        context?.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
        qwantbar?.setPrivacyMode(isPrivate)

        /* activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create(), "BROWSER_FRAGMENT")
            commit()
        } */
        (activity as BrowserActivity).showBrowserFragment()
        tabsClosedCallback?.invoke()
    }

    private fun updateTabCount() {
        if (sessionManager != null)
            reference.get()?.setCountWithAnimation(sessionManager!!.sessions.filter { it.private == isPrivate }.size)
    }
}
