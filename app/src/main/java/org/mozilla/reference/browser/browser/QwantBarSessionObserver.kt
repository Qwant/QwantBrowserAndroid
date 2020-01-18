package org.mozilla.reference.browser.browser

import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.storage.BookmarksStorage

class QwantBarSessionObserver(
        private val sessionManager: SessionManager,
        private val qwantbar: QwantBar,
        private val bookmarksStorage: BookmarksStorage
) : SessionManager.Observer, Session.Observer {
    private var textviewHome : View = qwantbar.findViewById(R.id.qwantbar_text_home)
    private var textviewBookmarks : View = qwantbar.findViewById(R.id.qwantbar_text_bookmarks)
    private var textviewTabs : View = qwantbar.findViewById(R.id.qwantbar_text_tabs)
    private var textviewMenu : View = qwantbar.findViewById(R.id.qwantbar_text_menu)
    private var textviewBack : View = qwantbar.findViewById(R.id.qwantbar_text_back)
    private var imageviewHome : ImageView = qwantbar.findViewById(R.id.qwantbar_button_home)

    private var currentMode: QwantBarMode = QwantBarMode.HOME

    init {
        sessionManager.sessions.forEach {
            it.register(this)
        }
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
            checkBookmarks(url)
        }
    }

    fun getCurrentMode() : QwantBarMode {
        return currentMode
    }

    private fun checkBookmarks(url: String) {
        if (qwantbar.getBookmarkButtonType() == QwantBar.BookmarkButtonType.SESSION) {
            if (bookmarksStorage.contains(url)) {
                qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.DELETE)
            } else {
                qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.ADD)
            }
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
            val selected = (sessionManager.selectedSession != null && sessionManager.selectedSession!!.url == imageviewHome.context.getString(R.string.homepage))
            imageviewHome.setImageResource(qwantbar.getIcon(QwantBar.QwantBarIcons.SEARCH, selected))
            qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)
            currentMode = QwantBarMode.HOME
        }
    }

    private fun setupNavigationBar() {
        if (currentMode != QwantBarMode.NAVIGATION) {
            this.setBarHeight(36)
            this.hideButtonsTexts()
            imageviewHome.setImageResource(qwantbar.getIcon(QwantBar.QwantBarIcons.HOME, false))
            qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.SESSION)
            currentMode = QwantBarMode.NAVIGATION
        }
    }

    private fun checkSession(session: Session) {
        checkSession(session.url)
        qwantbar.setPrivacyMode(session.private)
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

    override fun onFullScreenChanged(session: Session, enabled: Boolean) {
        qwantbar.visibility = if (enabled) View.GONE else View.VISIBLE
    }
}