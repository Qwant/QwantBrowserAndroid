package org.mozilla.reference.browser.tabs

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_qwant_tabs.*
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.concept.tabstray.Tab
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.layout.QwantBar
import java.lang.ref.WeakReference
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import mozilla.components.browser.state.selector.selectedTab


class QwantTabsFragment : Fragment(), UserInteractionHandler {
    private var tabsClosedCallback: (() -> Unit)? = null
    fun setTabsClosedCallback(tabsClosedCallback: (() -> Unit)) { this.tabsClosedCallback = tabsClosedCallback }
    private var isPrivate: Boolean = false
    fun setPrivacy(is_private: Boolean) { this.isPrivate = is_private }
    private var qwantbar: QwantBar? = null
    fun setQwantBar(bar: QwantBar) { this.qwantbar = bar }

    private var qwantTabsFeature: QwantTabsFeature? = null

    private var applicationContext: Context? = null
    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)

    private var tabsAdapter: TabsAdapter? = null
    private var tabsList: ListView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (activity != null) {
            applicationContext = requireActivity().applicationContext
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_qwant_tabs, container, false)
        tabsList = view.findViewById(R.id.tabsList)
        return view
    }

    private fun refreshFragment() {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager != null) {
            val currentFragment = fragmentManager.findFragmentByTag("TABS_FRAGMENT")
            val detachTransaction = fragmentManager.beginTransaction()
            val attachTransaction = fragmentManager.beginTransaction()

            currentFragment?.let {
                detachTransaction.detach(it)
                attachTransaction.attach(it)
                detachTransaction.commit()
                attachTransaction.commit()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        reference = WeakReference(tab_switch_normal_counter)

        tab_switch_button_background.setOnClickListener((View.OnClickListener {
            this.isPrivate = !isPrivate
            context.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
            qwantbar?.setPrivacyMode(isPrivate)
            this.setupPrivacyUi()
            this.refreshFragment()
        }))

        back_tabs_button.setOnClickListener { this.onBackPressed() }

        button_new_tab.setOnClickListener((View.OnClickListener {
            context.components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(applicationContext!!), selectTab = true, private = this.isPrivate)
            this.closeTabsTray()
        }))

        button_delete_all_tabs.setOnClickListener {
            val title = if (isPrivate) context.getString(R.string.menu_action_close_tabs_private) else context.getString(R.string.menu_action_close_tabs)
            val message = if (isPrivate) context.getString(R.string.close_tabs_confirm_private) else context.getString(R.string.close_tabs_confirm)
            val successToast = if (isPrivate) context.getString(R.string.close_tabs_done_private) else context.getString(R.string.close_tabs_done)
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    if (isPrivate) context.components.useCases.tabsUseCases.removePrivateTabs.invoke()
                    else context.components.useCases.tabsUseCases.removeNormalTabs.invoke()
                    Toast.makeText(context, successToast, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(android.R.string.no, null).show()
        }

        this.setupPrivacyUi()

        qwantTabsFeature = QwantTabsFeature(requireComponents.core.store, ::tabsChanged) {
            tabSessionState -> tabSessionState.content.private == isPrivate
        }
    }

    override fun onStart() {
        super.onStart()
        qwantTabsFeature?.start()
    }

    override fun onStop() {
        super.onStop()
        qwantTabsFeature?.stop()
    }

    private fun setupPrivacyUi() {
        tabsAdapter = TabsAdapter(requireContext(), ::tabSelected)
        tabsList?.adapter = tabsAdapter

        val context = requireContext()

        if (isPrivate) {
            // button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.button_background_private)
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons_custom_privacy_fill_small, 0, 0, 0)
            tab_switch_normal_counter.background = null
            tab_switch_normal_counter.elevation = 0F
            tab_switch_private_browsing_icon.background = ContextCompat.getDrawable(context, R.drawable.purple_gradient)
            tab_switch_private_browsing_icon.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
            button_new_tab.text = getString(R.string.menu_action_add_tab_private)
        } else {
            // button_new_tab.background = ContextCompat.getDrawable(context, R.drawable.button_background)
            // TODO change icon ?
            button_new_tab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons_system_add_circle_line, 0, 0, 0)
            // tab_switch_normal_counter.background = ???
            tab_switch_normal_counter.elevation = 6.dpToPx(Resources.getSystem().displayMetrics).toFloat()
            tab_switch_private_browsing_icon.background = null
            tab_switch_private_browsing_icon.elevation = 0F
            button_new_tab.text = getString(R.string.menu_action_add_tab)
        }

        this.updateTabCount()
    }

    private fun tabSelected(tab: Tab?) {
        if (tab != null) {
            requireComponents.useCases.tabsUseCases.selectTab.invoke(tab.id)
            this.closeTabsTray()
        }
    }

    private fun tabsChanged(tabs: Tabs) {
        tabsAdapter?.tabChanged(tabs)
        this.updateTabCount()
    }

    override fun onBackPressed(): Boolean {
        isPrivate = requireComponents.core.store.state.selectedTab?.content?.private ?: false
        closeTabsTray()
        return true
    }

    private fun closeTabsTray() {
        context?.setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
        Log.d("QWANT_BROWSER", "Set privacy from TabsFragment:closeTabsTray")
        // qwantbar?.visibility = View.VISIBLE
        qwantbar?.setPrivacyMode(isPrivate)
        qwantbar?.updateTabCount()
        (activity as BrowserActivity).showBrowserFragment()
        tabsClosedCallback?.invoke()

    }

    private fun updateTabCount() {
        reference.get()?.setCountWithAnimation(requireComponents.core.store.state.getNormalOrPrivateTabs(false).size)
        qwantbar?.updateTabCount(isPrivate)
    }
}