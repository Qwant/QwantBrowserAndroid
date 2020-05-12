package org.mozilla.reference.browser.browser

import android.content.Context
import android.util.Log
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.ext.toTabSessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.session.EngineViewPresenter
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents

/**
 * Feature implementation for connecting the engine module with the session module.
 */
class QwantSessionFeature(
        private val context: Context,
        private val sessionManager: SessionManager,
        private val sessionUseCases: SessionUseCases,
        private val goBackUseCase: SessionUseCases.GoBackUseCase,
        private val tabsUseCases: TabsUseCases,
        private val engineView: EngineView,
        private val sessionId: String? = null
) : LifecycleAwareFeature, UserInteractionHandler {
    internal val presenter = EngineViewPresenter(sessionManager, engineView, sessionId)

    /**
     * @deprecated Pass [SessionUseCases.GoBackUseCase] directly instead.
     */
    constructor(
            context: Context,
            sessionManager: SessionManager,
            sessionUseCases: SessionUseCases,
            tabsUseCases: TabsUseCases,
            engineView: EngineView,
            sessionId: String? = null
    ) : this(context, sessionManager, sessionUseCases, sessionUseCases.goBack, tabsUseCases, engineView, sessionId)

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
        val session = sessionId?.let {
            sessionManager.findSessionById(it)
        } ?: sessionManager.selectedSession

        if (engineView.canClearSelection()) {
            engineView.clearSelection()
            return true
        } else if (session?.canGoBack == true) {
            goBackUseCase(session)
            return true
        } else {
            if (sessionManager.sessions.size > 1) {
                if (session != null) {
                    if (session.hasParentSession) {
                        val parentId = session.toTabSessionState().parentId
                        sessionManager.sessions.forEach {
                            if (it.id == parentId) {
                                sessionManager.select(it)
                                tabsUseCases.removeTab(session)
                                return true
                            }
                        }
                    } else {
                        val currentSessionPrivate = session.private
                        sessionManager.sessions.forEach {
                            if (it.private == currentSessionPrivate && it.url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
                                sessionManager.select(it)
                                tabsUseCases.removeTab(session)
                                return true
                            }
                        }
                        sessionUseCases.loadUrl(QwantUtils.getHomepage(context.applicationContext))
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
