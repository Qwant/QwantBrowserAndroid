package org.mozilla.reference.browser.browser

import android.content.Context
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.ext.toTabSessionState
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
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
        private val sessionManager: SessionManager,
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
        val tab = store.state.findTabOrCustomTabOrSelectedTab(tabId)

        if (engineView.canClearSelection()) {
            engineView.clearSelection()
            return true
        } else if (tab?.content?.canGoBack == true) {
            useCases.sessionUseCases.goBack(tab.id)
            return true
        } else {
            val session = tabId?.let {
                sessionManager.findSessionById(it)
            } ?: sessionManager.selectedSession

            if (sessionManager.sessions.size > 1) {
                if (session != null) {
                    if (session.hasParentSession) {
                        val parentId = session.toTabSessionState().parentId
                        sessionManager.sessions.forEach {
                            if (it.id == parentId) {
                                sessionManager.select(it)

                                // val engineSession = it.toCustomTabSessionState().engineState.engineSession
                                // if (engineSession != null) engineView.render(engineSession)

                                useCases.tabsUseCases.removeTab(session.id)
                                return true
                            }
                        }
                    } else {
                        val currentSessionPrivate = session.private
                        sessionManager.sessions.forEach {
                            if (it.private == currentSessionPrivate && it.url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
                                sessionManager.select(it)
                                useCases.tabsUseCases.removeTab(session.id)
                                return true
                            }
                        }

                        useCases.sessionUseCases.loadUrl(QwantUtils.getHomepage(context.applicationContext))
                        return true
                    }
                }
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