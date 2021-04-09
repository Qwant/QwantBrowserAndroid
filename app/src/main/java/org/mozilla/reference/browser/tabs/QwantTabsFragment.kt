package org.mozilla.reference.browser.tabs

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tabstray.*
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.tabstray.Tab
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.layout.QwantBar
import java.lang.ref.WeakReference

class QwantTabsFragment : Fragment(), UserInteractionHandler {
    private var tabsClosedCallback: (() -> Unit)? = null
    fun setTabsClosedCallback(tabsClosedCallback: (() -> Unit)) { this.tabsClosedCallback = tabsClosedCallback }
    private var isPrivate: Boolean = false
    fun setPrivacy(is_private: Boolean) { this.isPrivate = is_private }
    private var qwantbar: QwantBar? = null
    fun setQwantBar(bar: QwantBar) { this.qwantbar = bar }

    private var sessionManager: SessionManager? = null
    private var applicationContext: Context? = null
    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)

    private var tabsAdapter: TabsAdapter? = null
    private var tabsList: ListView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (activity != null) {
            applicationContext = requireActivity().applicationContext
            sessionManager = requireActivity().application.components.core.sessionManager
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_qwant_tabs, container, false)

        tabsList = view.findViewById(R.id.tabsList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        reference = WeakReference(tab_switch_normal_counter)

        val tabCounterBox: ImageView = tab_switch_normal_counter.findViewById(R.id.counter_box)
        val tabCounterText: TextView = tab_switch_normal_counter.findViewById(R.id.counter_text)
        val tabColor = ContextCompat.getColor(context, R.color.photonWhite)
        tabCounterBox.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, tabColor))
        tabCounterText.setTextColor(tabColor)

        back_tabs_button.setOnClickListener((View.OnClickListener {
            this.onBackPressed()
        }))

        tab_switch_button_background.setOnClickListener((View.OnClickListener {
            this.isPrivate = !isPrivate
            context.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
            qwantbar?.setPrivacyMode(isPrivate)
            this.setupPrivacyUi()
        }))

        button_new_tab.setOnClickListener((View.OnClickListener {
            if (applicationContext != null) {
                if (isPrivate) {
                    context.components.useCases.tabsUseCases.addPrivateTab.invoke(QwantUtils.getHomepage(applicationContext!!))
                } else {
                    context.components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(applicationContext!!))
                }
                this.closeTabsTray()
            }
        }))

        tab_menu_more.setColorFilter(ContextCompat.getColor(context, R.color.menu_items))
        tab_menu_more.menuBuilder = BrowserMenuBuilder(listOf(
                BrowserMenuImageText(
                        context.getString(R.string.menu_action_close_tabs),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.icon_cross
                ) {
                    context.components.useCases.tabsUseCases.removeAllTabs.invoke()
                    tabsAdapter?.tabChanged()
                }
        ))
        this.setupPrivacyUi()
    }

    private fun setupPrivacyUi() {
        tabsAdapter = TabsAdapter(requireContext(), isPrivate, ::tabSelected, ::tabDeleted)
        tabsList?.adapter = tabsAdapter

        val context = requireContext()

        if (isPrivate) {
            button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_privacy_mask_white, 0, 0, 0)
            tab_switch_normal_counter.background = null
            tab_switch_normal_counter.elevation = 0F
            tab_switch_private_browsing_icon.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            tab_switch_private_browsing_icon.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
        } else {
            button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.blue_gradient)
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mozac_ic_tab_new, 0, 0, 0)
            tab_switch_normal_counter.background = ContextCompat.getDrawable(context, R.drawable.tab_switch_foreground_normal)
            tab_switch_normal_counter.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
            tab_switch_private_browsing_icon.background = null
            tab_switch_private_browsing_icon.elevation = 0F
        }

        this.updateTabCount()
    }

    private fun tabSelected(tab: Tab?) {
        if (tab != null) {
            requireContext().components.useCases.tabsUseCases.selectTab(tab.id)
            this.closeTabsTray()
        }
    }

    private fun tabDeleted(tabSession: TabSessionState?) {
        if (tabSession != null) {
            val session = requireContext().components.core.sessionManager.findSessionById(tabSession.id)
            if (session != null) {
                requireContext().components.core.sessionManager.remove(session)
                tabsAdapter?.tabChanged()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        closeTabsTray()
        return true
    }

    private fun closeTabsTray() {
        isPrivate = (sessionManager != null && sessionManager!!.selectedSession != null && sessionManager!!.selectedSession!!.private)
        context?.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
        qwantbar?.setPrivacyMode(isPrivate)
        qwantbar?.updateTabCount()
        (activity as BrowserActivity).showBrowserFragment()
        tabsClosedCallback?.invoke()
    }

    private fun updateTabCount() {
        if (sessionManager != null)
            reference.get()?.setCountWithAnimation(sessionManager!!.sessions.filter { it.private == isPrivate }.size)
        qwantbar?.updateTabCount(isPrivate)
    }
}