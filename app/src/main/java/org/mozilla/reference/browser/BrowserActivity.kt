/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import mozilla.components.browser.session.Session
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.utils.SafeIntent
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.browser.QwantBarSessionObserver
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.qwant.Analytics
import org.mozilla.reference.browser.settings.SettingsContainerFragment
import org.mozilla.reference.browser.storage.BookmarksFragment
import org.mozilla.reference.browser.storage.BookmarksStorage
import org.mozilla.reference.browser.tabs.TabsTrayFragment
import org.mozilla.reference.browser.tabs.tray.BrowserTabsTray
import java.util.*


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
        this.loadLocale()

        setContentView(R.layout.activity_main)

        super.onCreate(savedInstanceState)

        bookmarksStorage = BookmarksStorage(applicationContext)
        bookmarksStorage!!.restore()

        qwantbarSessionObserver = QwantBarSessionObserver(this, components.core.sessionManager, qwantbar, bookmarksStorage!!)
        components.core.sessionManager.register(this.qwantbarSessionObserver!!)

        qwantbar.setBookmarkStorage(bookmarksStorage!!)
        qwantbar.onTabsClicked(::showTabs)
        qwantbar.onBookmarksClicked(::showBookmarks)
        qwantbar.onHomeClicked(::showHome)
        qwantbar.onMenuClicked(::showSettings)

        if (savedInstanceState == null) {
            if (intent.action == "CHANGED_LANGUAGE") {
                qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.container, SettingsContainerFragment(::bookmarksOrTabsOrSettingsClosed, true), "SETTINGS_FRAGMENT")
                    commit()
                }
            } else {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.container, createBrowserFragment(sessionId), "BROWSER_FRAGMENT")
                    commit()
                }
            }
        }

        checkVersion()
    }

    private fun checkVersion() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val savedVersion = prefs.getString(getString(R.string.pref_key_saved_version), "undefined")

        val currentVersion = BuildConfig.VERSION_CODE.toString()

        var event: String? = null
        if (savedVersion == "undefined") {
            event = if (currentVersion.length > 7 && currentVersion.substring(0, 7) == getString(R.string.huawei_preinstalled_versioncode)) {
                "first_launch_huawei_preinstall"
            } else {
                "first_launch"
            }
        } else if (savedVersion != currentVersion) {
            event = "update"
        }

        if (event != null) {
            Analytics().execute(event, currentVersion)

            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putString(resources.getString(R.string.pref_key_saved_version), currentVersion)
            editor.apply()
        }
    }

    private fun loadLocale() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var savedLocale = prefs.getString(resources.getString(R.string.pref_key_general_language_interface), "undefined")

        if (savedLocale == "undefined") {
            // First time, set default locale for interface and search
            var defaultLocale = "en_US" // Fallback to english, as android does
            val phoneLocale = resources.configuration.locale.language
            val phoneCountry = resources.configuration.locale.country

            val availableLocale = resources.getStringArray(R.array.languages_interface_keys)
            if (!availableLocale.contains("${phoneLocale}_$phoneCountry")) {
                availableLocale.forEach { l -> if (l.startsWith(phoneLocale)) defaultLocale = l }
            } else {
                defaultLocale = "${phoneLocale}_$phoneCountry"
            }

            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putString(resources.getString(R.string.pref_key_general_language_interface), defaultLocale)
            // Set also search language
            if (prefs.getString(resources.getString(R.string.pref_key_general_language_search), "undefined") == "undefined") {
                editor.putString(resources.getString(R.string.pref_key_general_language_search), defaultLocale)
            }
            editor.apply()

            savedLocale = defaultLocale
        }
        if (savedLocale != resources.configuration.locale.language + "_" + resources.configuration.locale.country) {
            // Locale has been changed by preferences system, so it's already saved. Just update configuration
            resources.configuration.locale = Locale(savedLocale!!.split('_')[0], savedLocale.split('_')[1])
            Locale.setDefault(resources.configuration.locale)
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        }
    }

    override fun onBackPressed() {
        if (components.core.sessionManager.selectedSession == null || components.core.sessionManager.selectedSession!!.url.startsWith(getString(R.string.settings_page_startwith_filter))) {
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
                BrowserTabsTray(context, attrs)/* .also { tray ->
                    TabsTouchHelper(tray.tabsAdapter).attachToRecyclerView(tray)
                } */
            }
            else -> super.onCreateView(parent, name, context, attrs)
        }


    private fun showTabs() {
        this.supportFragmentManager.beginTransaction().apply {
            var isPrivate = false
            if (components.core.sessionManager.selectedSession != null)
                isPrivate = components.core.sessionManager.selectedSession!!.private
            replace(R.id.container, TabsTrayFragment(applicationContext, ::bookmarksOrTabsOrSettingsClosed, isPrivate, qwantbar), "TABS_FRAGMENT")
            commit()
        }
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.TABS)
    }

    private fun showBookmarks() {
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, BookmarksFragment(bookmarksStorage!!, ::bookmarksOrTabsOrSettingsClosed), "BOOKMARKS_FRAGMENT")
            commit()
        }
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.BOOKMARKS)
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
        if (session == null || !session.url.startsWith(getString(R.string.homepage_startwith_filter))) {
            var alreadyThere = false
            val currentSessionPrivate = (session != null && session.private)
            components.core.sessionManager.sessions.forEach {
                if (it.private == currentSessionPrivate && it.url.startsWith(getString(R.string.homepage_startwith_filter))) {
                    components.core.sessionManager.select(it)
                    alreadyThere = true
                }
            }
            if (!alreadyThere)
                components.useCases.sessionUseCases.loadUrl(QwantUtils.getHomepage(applicationContext))
        } else {
            components.useCases.sessionUseCases.loadUrl(QwantUtils.getHomepage(applicationContext))
        }

        this.showBrowserFragment()

        qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
    }

    private fun showSettings() {
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, SettingsContainerFragment(::bookmarksOrTabsOrSettingsClosed), "SETTINGS_FRAGMENT")
            commit()
        }
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
    }

    private fun bookmarksOrTabsOrSettingsClosed() {
        val session: Session? = components.core.sessionManager.selectedSession
        if (session == null || session.url.startsWith(baseContext.getString(R.string.homepage_base))) {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            qwantbarSessionObserver?.setupHomeBar()
        } else {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.NONE)
            qwantbarSessionObserver?.setupNavigationBar()
        }
        qwantbar.updateHomeIcon(qwantbarSessionObserver?.getCurrentMode())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.bookmarksStorage?.persist()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        this.bookmarksStorage?.restore()
    }
}
