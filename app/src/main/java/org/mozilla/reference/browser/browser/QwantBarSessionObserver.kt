package org.mozilla.reference.browser.browser

import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.View
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R

class QwantBarSessionObserver(
        private val sessionManager: SessionManager,
        private val qwantbar: View
) : SessionManager.Observer, Session.Observer {
    private var layoutHome : View = qwantbar.findViewById(R.id.qwantbar_layout_home)
    private var layoutBack : View = qwantbar.findViewById(R.id.qwantbar_layout_back)
    private var textviewHome : View = qwantbar.findViewById(R.id.qwantbar_text_home)
    private var textviewBookmarks : View = qwantbar.findViewById(R.id.qwantbar_text_bookmarks)
    private var textviewTabs : View = qwantbar.findViewById(R.id.qwantbar_text_tabs)
    private var textviewMenu : View = qwantbar.findViewById(R.id.qwantbar_text_menu)
    private var textviewBack : View = qwantbar.findViewById(R.id.qwantbar_text_back)

    private var currentMode: QwantBarMode? = null

    init {
        sessionManager.sessions.forEach {
            it.register(this)
        }
        currentMode = QwantBarMode.HOME
        checkSession()
    }

    enum class QwantBarMode {
        HOME, NAVIGATION
    }

    private fun checkSession(url: String) {
        if (url.contains("https://www.qwant.com")) {
            setupHomeBar()
        } else {
            setupNavigationBar()
        }
    }

    private fun showButtonsTexts() {
        textviewHome.visibility = View.VISIBLE
        textviewBookmarks.visibility = View.VISIBLE
        textviewTabs.visibility = View.VISIBLE
        textviewMenu.visibility = View.VISIBLE
        textviewBack.visibility = View.VISIBLE
    }

    private fun hideButtonsTexts() {
        textviewHome.visibility = View.GONE
        textviewBookmarks.visibility = View.GONE
        textviewTabs.visibility = View.GONE
        textviewMenu.visibility = View.GONE
        textviewBack.visibility = View.GONE
    }

    private fun setBarHeight(height_dp: Int) {
        val qwantbarParams = qwantbar.layoutParams
        qwantbarParams.height = height_dp.dpToPx(Resources.getSystem().displayMetrics)
        qwantbar.layoutParams = qwantbarParams
    }

    private fun setupHomeBar() {
        if (currentMode != QwantBarMode.HOME) {
            this.setBarHeight(56)
            this.showButtonsTexts()
            currentMode = QwantBarMode.HOME
        }
    }

    private fun setupNavigationBar() {
        if (currentMode != QwantBarMode.NAVIGATION) {
            this.setBarHeight(36)
            this.hideButtonsTexts()
            currentMode = QwantBarMode.NAVIGATION
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
        checkSession(session)
    }

    override fun onLaunchIntentRequest(session: Session, url: String, appIntent: Intent?) {
        checkSession(url)
    }
}