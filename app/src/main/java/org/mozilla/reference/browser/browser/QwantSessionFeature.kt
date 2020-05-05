package org.mozilla.reference.browser.browser

import android.util.Log
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.session.EngineViewPresenter
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.ext.components

/**
 * Feature implementation for connecting the engine module with the session module.
 */
class QwantSessionFeature(
        private val sessionManager: SessionManager,
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
            sessionManager: SessionManager,
            sessionUseCases: SessionUseCases,
            tabsUseCases: TabsUseCases,
            engineView: EngineView,
            sessionId: String? = null
    ) : this(sessionManager, sessionUseCases.goBack, tabsUseCases, engineView, sessionId)

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

        when {
            engineView.canClearSelection() -> {
                engineView.clearSelection()
                return true
            }

            session?.canGoBack == true -> {
                goBackUseCase(session)
                return true
            } else -> {
                if (sessionManager.sessions.size > 1) {
                    session?.let { tabsUseCases.removeTab(it) }

                    // TODO should know if session is from assist or external, so we can go back to assist or close the app ...

                    return true
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
