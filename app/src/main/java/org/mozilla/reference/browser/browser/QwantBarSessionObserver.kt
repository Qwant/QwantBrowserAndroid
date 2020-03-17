package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.storage.BookmarksStorage

class QwantBarSessionObserver(
        private val context: Context,
        private val sessionManager: SessionManager,
        private val qwantbar: QwantBar,
        private val bookmarksStorage: BookmarksStorage
) : SessionManager.Observer, Session.Observer {
    private var textviewHome : TextView = qwantbar.findViewById(R.id.qwantbar_text_home)
    private var textviewBookmarks : View = qwantbar.findViewById(R.id.qwantbar_text_bookmarks)
    private var textviewTabs : View = qwantbar.findViewById(R.id.qwantbar_text_tabs)
    private var textviewMenu : View = qwantbar.findViewById(R.id.qwantbar_text_menu_qwant)
    private var imageviewHome : ImageView = qwantbar.findViewById(R.id.qwantbar_button_home)
    private var imageviewNavBack : ImageView = qwantbar.findViewById(R.id.qwantbar_button_nav_back)
    private var imageviewNavForward : ImageView = qwantbar.findViewById(R.id.qwantbar_button_nav_forward)

    private var layoutNavBack : View = qwantbar.findViewById(R.id.qwantbar_layout_nav_back)
    private var layoutNavForward : View = qwantbar.findViewById(R.id.qwantbar_layout_nav_forward)
    private var layoutBookmarks : View = qwantbar.findViewById(R.id.qwantbar_layout_bookmarks)
    private var layoutMenuQwant : View = qwantbar.findViewById(R.id.qwantbar_layout_menu_qwant)
    private var layoutMenuNav : View = qwantbar.findViewById(R.id.qwantbar_layout_menu_nav)

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
        if (url.startsWith(context.getString(R.string.homepage_base))) { // include result page too
            setupHomeBar()
        } else {
            setupNavigationBar()
        }
    }

    fun getCurrentMode() : QwantBarMode {
        return currentMode
    }

    private fun showButtonsTexts() {
        textviewHome.visibility = View.VISIBLE
        textviewBookmarks.visibility = View.VISIBLE
        textviewTabs.visibility = View.VISIBLE
        textviewMenu.visibility = View.VISIBLE
    }

    private fun hideButtonsTexts() {
        textviewHome.visibility = View.GONE
        textviewBookmarks.visibility = View.GONE
        textviewTabs.visibility = View.GONE
        textviewMenu.visibility = View.GONE
    }

    private fun setBarHeight(height_dp: Int) {
        val qwantbarParams = qwantbar.layoutParams
        qwantbarParams.height = height_dp.dpToPx(Resources.getSystem().displayMetrics)
        qwantbar.layoutParams = qwantbarParams
    }

    fun setupHomeBar() {
        if (currentMode != QwantBarMode.HOME) {
            this.setBarHeight(56)
            this.showButtonsTexts()
            val selected = (sessionManager.selectedSession == null || sessionManager.selectedSession!!.url.startsWith("https://www.qwant.com"))
            imageviewHome.setImageResource(qwantbar.getIcon(QwantBar.QwantBarIcons.SEARCH, selected))
            if (selected) qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            // qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)

            layoutNavBack.visibility = View.GONE
            layoutNavForward.visibility = View.GONE
            layoutBookmarks.visibility = View.VISIBLE
            layoutMenuQwant.visibility = View.VISIBLE
            layoutMenuNav.visibility = View.GONE

            currentMode = QwantBarMode.HOME
        }
    }

    fun setupNavigationBar() {
        if (currentMode != QwantBarMode.NAVIGATION) {
            this.setBarHeight(36)
            this.hideButtonsTexts()
            imageviewHome.setImageResource(qwantbar.getIcon(QwantBar.QwantBarIcons.HOME, false))
            // qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.SESSION)

            layoutNavBack.visibility = View.VISIBLE
            layoutNavForward.visibility = View.VISIBLE
            layoutBookmarks.visibility = View.GONE
            layoutMenuQwant.visibility = View.GONE
            layoutMenuNav.visibility = View.VISIBLE

            currentMode = QwantBarMode.NAVIGATION
        }
    }

    private fun checkSession(session: Session) {
        checkSession(session.url)
        qwantbar.setPrivacyMode(session.private)
        context.setTheme(if (session.private) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
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

    override fun onNavigationStateChanged(session: Session, canGoBack: Boolean, canGoForward: Boolean) {
        super.onNavigationStateChanged(session, canGoBack, canGoForward)
        /* if (sessionManager.selectedSession != null && session.id == sessionManager.selectedSession!!.id) {
            val backColor = if (canGoBack) context.getColorFromAttr(R.attr.qwant_color_main) else context.getColorFromAttr(R.attr.qwant_color_light)
            imageviewNavBack.setBackgroundColor(backColor)
            val forwardColor = if (canGoForward) context.getColorFromAttr(R.attr.qwant_color_main) else context.getColorFromAttr(R.attr.qwant_color_light)
            imageviewNavForward.setBackgroundColor(forwardColor)
        } */
    }
}