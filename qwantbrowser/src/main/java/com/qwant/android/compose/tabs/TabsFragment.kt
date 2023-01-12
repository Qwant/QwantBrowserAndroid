package com.qwant.android.compose.tabs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.layout.QwantBar


class TabsFragment : Fragment(), UserInteractionHandler {
    private var tabsClosedCallback: (() -> Unit)? = null
    fun setTabsClosedCallback(tabsClosedCallback: (() -> Unit)) { this.tabsClosedCallback = tabsClosedCallback }
    private var qwantbar: QwantBar? = null
    fun setQwantBar(bar: QwantBar) { this.qwantbar = bar }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val a = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MaterialTheme {
                    TabsView(
                        store = context.components.core.store,
                        tabsUseCases = context.components.useCases.tabsUseCases,
                        thumbnailStorage = context.components.core.thumbnailStorage,
                        homepageUrl = QwantUtils.getHomepage(context),
                        onClose = { closeTabs() }
                    )
                }
            }
        }
        return a
    }

    override fun onBackPressed(): Boolean {
        this.closeTabs()
        return true
    }

    private fun closeTabs() {
        val isSelectedPrivate = requireComponents.core.store.state.selectedTab?.content?.private ?: false
        context?.setTheme(if (isSelectedPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
        qwantbar?.setPrivacyMode(isSelectedPrivate)
        qwantbar?.updateTabCount()
        (activity as BrowserActivity).showBrowserFragment()
        tabsClosedCallback?.invoke()
    }
}