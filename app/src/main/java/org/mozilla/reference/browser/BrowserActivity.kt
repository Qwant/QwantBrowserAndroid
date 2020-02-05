/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import mozilla.components.browser.session.Session
import mozilla.components.browser.tabstray.BrowserTabsTray
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.utils.SafeIntent
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.browser.QwantBarSessionObserver
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.storage.BookmarksFragment
import org.mozilla.reference.browser.storage.BookmarksStorage
import org.mozilla.reference.browser.tabs.TabsTouchHelper
import org.mozilla.reference.browser.tabs.TabsTrayFragment


/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : AppCompatActivity() {
    private var bookmarksStorage: BookmarksStorage? = null
    private var qwantbarSessionObserver: QwantBarSessionObserver? = null
    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?): Fragment =
        BrowserFragment.create(sessionId)

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeId = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", R.style.ThemeQwantNoActionBar)
        setTheme(themeId)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, createBrowserFragment(sessionId))
                commit()
            }
        }

        bookmarksStorage = BookmarksStorage(applicationContext)
        bookmarksStorage!!.restore()

        qwantbar.setBookmarkStorage(bookmarksStorage!!)
        qwantbar.onTabsClicked(::showTabs)
        qwantbar.onBookmarksClicked(::showBookmarks)
        qwantbar.onHomeClicked(::showHome)
        qwantbar.onBackClicked(::onBackPressed)
        qwantbar.onMenuClicked(::showSettings)

        qwantbarSessionObserver = QwantBarSessionObserver(components.core.sessionManager, qwantbar, bookmarksStorage!!)
        components.core.sessionManager.register(this.qwantbarSessionObserver!!)

    }

    override fun onBackPressed() {
        if (components.core.sessionManager.selectedSession != null && components.core.sessionManager.selectedSession!!.url == getString(R.string.settings_page)) {
            qwantbar.setLeftButton(QwantBar.LeftButtonType.HOME)
            qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
        }

        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            }
        }

        super.onBackPressed()

        removeSessionIfNeeded()
    }

    /**
     * If needed remove the current session.
     *
     * If a session is a custom tab or was opened from an external app then the session gets removed once you go back
     * to the third-party app.
     *
     * Eventually we may want to move this functionality into one of our feature components.
     */
    private fun removeSessionIfNeeded() {
        val sessionManager = components.core.sessionManager
        val sessionId = sessionId

        val session = (if (sessionId != null) {
            sessionManager.findSessionById(sessionId)
        } else {
            sessionManager.selectedSession
        }) ?: return

        if (session.source == Session.Source.ACTION_VIEW || session.source == Session.Source.CUSTOM_TAB) {
            sessionManager.remove(session)
        }
    }

    override fun onUserLeaveHint() {
        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onHomePressed()) {
                return
            }
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            TabsTray::class.java.name -> {
                BrowserTabsTray(context, attrs).also { tray ->
                    TabsTouchHelper(tray.tabsAdapter).attachToRecyclerView(tray)
                }
            }
            else -> super.onCreateView(parent, name, context, attrs)
        }


    private fun showTabs() {
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, TabsTrayFragment(::bookmarksOrTabsClosed))
            commit()
        }
        qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)
        qwantbar.setHighlight(QwantBar.QwantBarSelection.TABS)
        qwantbar.setLeftButton(QwantBar.LeftButtonType.BACK)
    }

    private fun showBookmarks() {
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, BookmarksFragment(bookmarksStorage!!, ::bookmarksOrTabsClosed))
            commit()
        }
        qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)
        qwantbar.setHighlight(QwantBar.QwantBarSelection.BOOKMARKS)
        qwantbar.setLeftButton(QwantBar.LeftButtonType.BACK)
    }

    private fun showBrowserFragment() {
        var browserFragment = this.supportFragmentManager.findFragmentByTag("BROWSER_FRAGMENT")
        if (browserFragment == null || browserFragment.isHidden) {
            if (browserFragment == null) {
                browserFragment = BrowserFragment.create()
            }
            this.supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, browserFragment, "BROWSER_FRAGMENT")
                commit()
            }
        }
    }

    private fun showHome() {
        val session: Session? = components.core.sessionManager.selectedSession
        if (session == null || session.url != getString(R.string.homepage)) {
            var alreadyThere = false
            val currentSessionPrivate = (session != null && session.private)
            components.core.sessionManager.sessions.forEach {
                if (it.private == currentSessionPrivate && it.url == getString(R.string.homepage)) {
                    components.core.sessionManager.select(it)
                    alreadyThere = true
                }
            }
            if (!alreadyThere)
                components.useCases.sessionUseCases.loadUrl(getString(R.string.homepage))
        }

        this.showBrowserFragment()

        qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
        qwantbar.setLeftButton(QwantBar.LeftButtonType.HOME)
        qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)
    }

    private fun showSettings() {
       /* val session: Session? = components.core.sessionManager.selectedSession
        if (session == null || session.url != getString(R.string.settings_page)) {
            var alreadyThere = false
            components.core.sessionManager.sessions.forEach {
                if (it.url == getString(R.string.settings_page)) {
                    components.core.sessionManager.select(it)
                    alreadyThere = true
                }
            }
            if (!alreadyThere)
                components.useCases.sessionUseCases.loadUrl(getString(R.string.settings_page))
        } */ // TODO Put that back, but block user non settings
        components.useCases.sessionUseCases.loadUrl(getString(R.string.settings_page))

        this.showBrowserFragment()

        qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
        qwantbar.setLeftButton(QwantBar.LeftButtonType.BACK)
        qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)
    }

    private fun bookmarksOrTabsClosed() {
        val session: Session? = components.core.sessionManager.selectedSession
        if (session != null && session.url == getString(R.string.settings_page)) {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
            qwantbar.setLeftButton(QwantBar.LeftButtonType.BACK)
        } else if (session == null || session.url.contains("https://www.qwant.com")) {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            qwantbar.setLeftButton(QwantBar.LeftButtonType.HOME)
        } else {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.NONE)
            qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.SESSION)
            qwantbar.setLeftButton(QwantBar.LeftButtonType.HOME)
        }
        qwantbar.updateHomeIcon(qwantbarSessionObserver?.getCurrentMode())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("QWANT_BROWSER", "on save instance")
        super.onSaveInstanceState(outState)
        this.bookmarksStorage?.persist()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        Log.d("QWANT_BROWSER", "on restore instance")
        super.onRestoreInstanceState(savedInstanceState)
        this.bookmarksStorage?.restore()
    }
}
