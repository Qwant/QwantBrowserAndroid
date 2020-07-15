package org.mozilla.reference.browser.browser

import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R

class ToolbarSessionObserver(
        private val sessionManager: SessionManager,
        private val toolbar: View,
        private val swipeRefresh: SwipeRefreshLayout
) : SessionManager.Observer, Session.Observer {

    private var shownSplash = false

    init {
        sessionManager.sessions.forEach {
            it.register(this)
        }
        checkSession()
    }

    private fun checkSession(url: String) {
        if (url.startsWith(toolbar.context.getString(R.string.homepage_base))) {
            toolbar.visibility = View.GONE
            swipeRefresh.setPadding(0, 0, 0, 0)
        } else {
            toolbar.visibility = View.VISIBLE
            swipeRefresh.setPadding(0, 56.dpToPx(Resources.getSystem().displayMetrics), 0, 0)
        }
    }

    private fun checkSession(session: Session) {
        checkSession(session.url)
    }

    private fun checkSession() {
        if (sessionManager.selectedSession != null) this.checkSession(sessionManager.selectedSession!!)
    }

    //
    // SESSION MANAGER
    //
    override fun onSessionAdded(session: Session) {
        session.register(this)
    }
    override fun onSessionSelected(session: Session) {
        checkSession(session)
    }
    override fun onSessionsRestored() {
        checkSession()
    }

    //
    // SESSION
    //
    override fun onUrlChanged(session: Session, url: String) {
        checkSession(url)
    }

    override fun onLaunchIntentRequest(session: Session, url: String, appIntent: Intent?) {
        checkSession(url)
    }

    // override fun onProgress(session: Session, progress: Int) {
        /* if (session.url.startsWith(toolbar.context.getString(R.string.homepage_startwith_filter))) {
            if (progress < 100 && !shownSplash) {
                toggleSplash(true)
            } else if (progress == 100 && shownSplash) {
                toggleSplash(false)
            }
        } else if (shownSplash) {
            toggleSplash(false)
        } */
    // }

    private fun toggleSplash(is_shown: Boolean) {
        /* swipeRefresh.visibility = if (is_shown) View.INVISIBLE else View.VISIBLE
        shownSplash = is_shown */
    }
}