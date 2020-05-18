/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.SharedPreferences
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
import org.mozilla.reference.browser.settings.SettingsContainerFragment
import org.mozilla.reference.browser.storage.BookmarksFragment
import org.mozilla.reference.browser.storage.BookmarksStorage
import org.mozilla.reference.browser.tabs.TabsTrayFragment
import org.mozilla.reference.browser.tabs.tray.BrowserTabsTray
import java.util.*


/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : AppCompatActivity(), SettingsContainerFragment.OnSettingsClosed {
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
        this.fixHuaweiDefaultContentFilter() // TODO remove this

        setContentView(R.layout.activity_main)

        super.onCreate(savedInstanceState)

        bookmarksStorage = BookmarksStorage(applicationContext)
        bookmarksStorage?.restore()

        qwantbarSessionObserver = QwantBarSessionObserver(this, components.core.sessionManager, qwantbar)
        components.core.sessionManager.register(this.qwantbarSessionObserver!!)

        qwantbar.setBookmarkStorage(bookmarksStorage!!)
        qwantbar.onTabsClicked(::showTabs)
        qwantbar.onBookmarksClicked(::showBookmarks)
        qwantbar.onHomeClicked(::showHome)
        qwantbar.onMenuClicked(::showSettings)
        qwantbar.onBackClicked(::onBackPressed)

        if (savedInstanceState == null) {
            if (intent.action == "CHANGED_LANGUAGE") {
                qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.container, SettingsContainerFragment.create(true), "SETTINGS_FRAGMENT")
                    commit()
                }
            } else {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.container, createBrowserFragment(sessionId), "BROWSER_FRAGMENT")
                    commit()
                }
            }
        }
    }

    private fun loadLocale() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val phoneLocale = resources.configuration.locale.language
        val phoneCountry = resources.configuration.locale.country

        val prefEditor: SharedPreferences.Editor = prefs.edit()

        val availableCountries = resources.getStringArray(R.array.languages_search_keys)
        var savedSearchLanguage = prefs.getString(resources.getString(R.string.pref_key_general_language_search), "undefined")
        if (savedSearchLanguage == null || savedSearchLanguage == "undefined" || !availableCountries.contains(savedSearchLanguage)) {
            var searchLanguage = "GB" // Fallback to english, as android does
            if (availableCountries.contains(phoneCountry)) searchLanguage = phoneCountry
            prefEditor.putString(resources.getString(R.string.pref_key_general_language_search), searchLanguage)
            savedSearchLanguage = searchLanguage
        }

        val savedSearchLanguageIndex = availableCountries.indexOf(savedSearchLanguage)
        val listRegionsArrays = resources.obtainTypedArray(R.array.region_list_arrays_values_sr)
        val arrayIdValues = listRegionsArrays.getResourceId(savedSearchLanguageIndex, 0)
        if (arrayIdValues > 0) {
            val availableRegions = resources.getStringArray(arrayIdValues)
            val savedSearchRegion = prefs.getString(resources.getString(R.string.pref_key_general_region_search), "undefined")
            if (savedSearchRegion == null || savedSearchRegion == "undefined" || !availableRegions.contains(savedSearchRegion)) {
                var searchRegion = availableRegions[0]
                if (availableRegions.contains(phoneLocale)) searchRegion = phoneLocale
                prefEditor.putString(resources.getString(R.string.pref_key_general_region_search), searchRegion)
            }
        }
        listRegionsArrays.recycle()

        var savedInterfaceLanguage = prefs.getString(resources.getString(R.string.pref_key_general_language_interface), "undefined")
        val availableLocale = resources.getStringArray(R.array.languages_interface_keys)
        Log.d("QWANT_BROWSER", "saved interface language: $savedInterfaceLanguage")
        Log.d("QWANT_BROWSER", "phone language: ${phoneLocale}_$phoneCountry")
        if (savedInterfaceLanguage == "undefined" || !availableLocale.contains(savedInterfaceLanguage)) {
            // First time, set default locale for interface and search
            var interfaceLanguage = "en_GB" // Fallback to english, as android does

            if (!availableLocale.contains("${phoneLocale}_$phoneCountry")) {
                availableLocale.forEach { l -> if (l.startsWith(phoneLocale)) interfaceLanguage = l }
            } else {
                interfaceLanguage = "${phoneLocale}_$phoneCountry"
            }

            prefEditor.putString(resources.getString(R.string.pref_key_general_language_interface), interfaceLanguage)

            savedInterfaceLanguage = interfaceLanguage
        }
        if (savedInterfaceLanguage != resources.configuration.locale.language + "_" + resources.configuration.locale.country) {
            // Locale has been changed by preferences system, so it's already saved. But we need to update configuration
            resources.configuration.locale = Locale(savedInterfaceLanguage!!.split('_')[0], savedInterfaceLanguage.split('_')[1])
            Locale.setDefault(resources.configuration.locale)
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        }

        prefEditor.apply()
    }

    fun fixHuaweiDefaultContentFilter() {
        // TODO remove that at one point
        // force content filter to moderate if set to none on first launch
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstLaunch = prefs.getBoolean(resources.getString(R.string.pref_key_first_launch), true)
        if (firstLaunch) {
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putBoolean(resources.getString(R.string.pref_key_first_launch), false)
            val adultContentFilter = prefs.getString(resources.getString(R.string.pref_key_general_adultcontent), "undefined")
            if (adultContentFilter == "undefined" || adultContentFilter == resources.getString(R.string.settings_adultcontent_none_code)) {
                editor.putString(resources.getString(R.string.pref_key_general_adultcontent), resources.getString(R.string.settings_adultcontent_moderate_code))
            }
            editor.apply()
        }
    }

    override fun onBackPressed() {
        val sessionManager  = components.core.sessionManager
        if (sessionManager.selectedSession != null) {
            val url = components.core.sessionManager.selectedSession!!.url
            if (!sessionManager.selectedSession!!.canGoBack && sessionManager.selectedSession!!.source.name == "ACTION_VIEW") {
                // Tab has been opened from external app, so we close the app to get back to it, after closing the tab
                super.onBackPressed()
                sessionManager.remove(sessionManager.selectedSession!!)
                return
            } else if (url.startsWith(getString(R.string.homepage_startwith_filter)) && url.contains("&o=")) {
                // Fix for closing qwant opened medias with back button
                components.useCases.sessionUseCases.loadUrl(url.substringBefore("&o="))
                return
            } else if (components.core.sessionManager.selectedSession!!.url.startsWith(getString(R.string.settings_page_startwith_filter))) {
                // Highlight search icon when on the settings page. Do not return.
                qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            }
        }

        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            }
        }

        super.onBackPressed()
        removeSessionIfNeeded()
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is SettingsContainerFragment -> {
                fragment.setOnSettingsClosed(this)
            }
            is TabsTrayFragment -> {
                var isPrivate = false
                if (components.core.sessionManager.selectedSession != null)
                    isPrivate = components.core.sessionManager.selectedSession!!.private
                fragment.setPrivacy(isPrivate)
                fragment.setQwantBar(qwantbar)
                fragment.setTabsClosedCallback(::bookmarksOrTabsOrSettingsClosed)
            }
            is BookmarksFragment -> {
                if (bookmarksStorage != null) fragment.setBookmarkStorage(bookmarksStorage!!)
                fragment.setBookmarksClosedCallback(::bookmarksOrTabsOrSettingsClosed)
            }
        }
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
            replace(R.id.container, TabsTrayFragment(), "TABS_FRAGMENT")
            commit()
        }
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.TABS)
    }

    private fun showBookmarks() {
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, BookmarksFragment(), "BOOKMARKS_FRAGMENT")
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

    fun showSettings() {
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, SettingsContainerFragment.create(), "SETTINGS_FRAGMENT")
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

    override fun settingsClosed() {
        bookmarksOrTabsOrSettingsClosed()
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
