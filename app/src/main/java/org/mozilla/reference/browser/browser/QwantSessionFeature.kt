package org.mozilla.reference.browser.browser

import android.content.Context
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineView
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.components.UseCases

/**
 * Feature implementation for connecting the engine module with the session module.
 */
class QwantSessionFeature(
        private val context: Context,
        private val store: BrowserStore,
        private val useCases: UseCases,
        private val engineView: EngineView,
        private val tabId: String? = null
) : LifecycleAwareFeature, UserInteractionHandler {
    private val presenter = EngineViewPresenter(store, engineView, tabId)

    /**
     * Start feature: App is in the foreground.
     */
    override fun start() {
        presenter.start()
    }

    /**
     * Handler for back pressed events in activities that use this feature.
     *
     * @return true if the event was handled, otherwise false.
     */
    override fun onBackPressed(): Boolean {
        val tab = store.state.selectedTab

        if (engineView.canClearSelection()) {
            engineView.clearSelection()
            return true
        } else if (tab?.content?.canGoBack == true) {
            useCases.sessionUseCases.goBack(tab.id)
            return true
        } else {
            if (store.state.tabs.size > 1 && tab != null) {
                if (tab.parentId != null) {
                    useCases.tabsUseCases.selectTab(tab.parentId!!)
                    useCases.tabsUseCases.removeTab(tab.id)
                    return true
                }
            }
            if (tab?.content?.url?.startsWith(context.getString(R.string.homepage_startwith_filter), false) == false) {
                useCases.tabsUseCases.selectOrAddTab(QwantUtils.getHomepage(context.applicationContext), tab.content.private)
                useCases.tabsUseCases.removeTab(tab.id)
                return true
            }
        }

        return false
    }

    /**
     * Stop feature: App is in the background.
     */
    override fun stop() {
        presenter.stop()
    }
}