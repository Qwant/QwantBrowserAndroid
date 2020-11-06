/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.session.Session
import mozilla.components.browser.state.state.WebExtensionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.storage.PageObservation
import mozilla.components.concept.storage.PageVisit
import mozilla.components.concept.storage.RedirectSource
import mozilla.components.concept.storage.VisitType
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.webextensions.WebExtensionPopupFeature
import org.mozilla.gecko.GeckoProfile
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.browser.QwantBarSessionObserver
import org.mozilla.reference.browser.compat.*
import org.mozilla.reference.browser.compat.BrowserContract.Bookmarks
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.settings.SettingsContainerFragment
import org.mozilla.reference.browser.storage.BookmarkItemV1
import org.mozilla.reference.browser.storage.bookmarks.BookmarksFragment
import org.mozilla.reference.browser.storage.bookmarks.BookmarksStorage
import org.mozilla.reference.browser.storage.history.HistoryFragment
import org.mozilla.reference.browser.tabs.TabsTrayFragment
import org.mozilla.reference.browser.tabs.tray.BrowserTabsTray
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess


/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : AppCompatActivity(), SettingsContainerFragment.OnSettingsClosed {
    private var bookmarksStorage: BookmarksStorage? = null
    private var qwantbarSessionObserver: QwantBarSessionObserver? = null
    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    private var darkmode: Int = 0

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?): Fragment =
            BrowserFragment.create(sessionId)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("QWANT_BROWSER", "browser activity creation !!")
        PACKAGE_NAME = packageName

        this.loadLocale()

        darkmode = if (intent.hasExtra("newTheme")) {
            intent.getIntExtra("newTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }

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
        qwantbar.onHistoryClicked(::showHistory)
        qwantbar.onQuitAppClicked(::quitApp)

        if (savedInstanceState == null) {
            when (intent.action) {
                "CHANGED_LANGUAGE" -> {
                    Log.d("QWANT_BROWSER", "Browser activity recreated: changed language")

                    qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, SettingsContainerFragment.create(language_changed_reload = true, theme_changed_reload = false), "SETTINGS_FRAGMENT")
                        commit()
                    }
                    intent.action = null
                }
                "CHANGED_THEME" -> {
                    Log.d("QWANT_BROWSER", "Browser activity recreated: changed theme")

                    qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, SettingsContainerFragment.create(language_changed_reload = false, theme_changed_reload = true), "SETTINGS_FRAGMENT")
                        commit()
                    }
                    intent.action = null
                }
                else -> {
                    Log.d("QWANT_BROWSER", "Browser activity created, no subs")

                    Log.d("QWANT_BROWSER", "browser activity - onCreate - new browser fragment with session")
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, createBrowserFragment(sessionId), "BROWSER_FRAGMENT")
                        commit()
                    }
                }
            }
        }

        this.loadV35Db()
        qwantbar.updateTabCount()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (darkmode != (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
            val intent = Intent(applicationContext, BrowserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.action = "CHANGED_THEME"
            intent.putExtra("newTheme", newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK);
            startActivity(intent)
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

    fun loadV35Db() {
        // load old db to new one on first launch
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val firstLaunch40 = prefs.getBoolean(resources.getString(R.string.pref_key_first_launch_40), true)
        val firstLaunch402 = prefs.getBoolean(resources.getString(R.string.pref_key_first_launch_402), true)

        val db: BrowserDB? = LocalBrowserDB.from(GeckoProfile.get(applicationContext, null))
        val cr = applicationContext.contentResolver

        if (firstLaunch40) {
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putBoolean(resources.getString(R.string.pref_key_first_launch_40), false)
            editor.apply()

            val bookmarkCursor = db?.getBookmarksInFolder(cr, Bookmarks.FIXED_ROOT_ID.toLong())
            try {
                if (bookmarkCursor != null) {
                    while (bookmarkCursor.moveToNext()) {
                        val title: String = bookmarkCursor.getString(bookmarkCursor.getColumnIndexOrThrow(Bookmarks.TITLE))
                        val url: String = bookmarkCursor.getString(bookmarkCursor.getColumnIndexOrThrow(Bookmarks.URL))
                        bookmarksStorage?.addBookmark(BookmarkItemV1(title, url))
                    }
                }
            } catch (e: Exception) {
                Log.e("QWANT_BROWSER", "exception: ${e.message}\n${e.stackTrace}")
            }

            val historyCursor = db?.getAllVisitedHistory(cr)
            try {
                if (historyCursor != null) {
                    while (historyCursor.moveToNext()) {
                        val title: String = historyCursor.getString(historyCursor.getColumnIndexOrThrow(BrowserContract.History.TITLE))
                        val url: String = historyCursor.getString(historyCursor.getColumnIndexOrThrow(BrowserContract.History.URL))
                        val time: Long = historyCursor.getLong(historyCursor.getColumnIndexOrThrow(BrowserContract.History.DATE_LAST_VISITED))
                        MainScope().launch {
                            applicationContext.components.core.historyStorage.recordVisit(url, PageVisit(VisitType.LINK, RedirectSource.NOT_A_SOURCE), time)
                            applicationContext.components.core.historyStorage.recordObservation(url, PageObservation(title = title))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("QWANT_BROWSER", "exception: ${e.message}\n${e.stackTrace}")
            }
        }

        if (firstLaunch402) {
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putBoolean(resources.getString(R.string.pref_key_first_launch_402), false)
            editor.apply()

            restoreSessionTabs()
        }
    }

    private val SESSION_FILE = "sessionstore.js"

    private fun readSessionFile(): String? {
        val sessionFile: File? = File(GeckoProfile.get(applicationContext, null).dir, SESSION_FILE)
        try {
            if (sessionFile != null && sessionFile.exists()) {
                val fr = FileReader(sessionFile)

                return fr.use { fr ->
                    val sb = StringBuilder()
                    val buf = CharArray(8192)
                    var read = fr.read(buf)
                    while (read >= 0) {
                        sb.append(buf, 0, read)
                        read = fr.read(buf)
                    }
                    sb.toString()
                }
            }
        } catch (ioe: IOException) {
            Log.e("QWANT_BROWSER", "Unable to read session file", ioe)
        }
        return null
    }


    private class LastSessionParser(private val context: Context) : SessionParser() {
        override fun onTabRead(sessionTab: SessionTab) {
            if (sessionTab.url != null && sessionTab.url != "null" && !sessionTab.url.startsWith("https://www.qwant.com/?client=qwantbrowser")) {
                context.components.useCases.tabsUseCases.addTab(sessionTab.url, selectTab = false, startLoading = false)
            }
        }
    }

    private fun restoreSessionTabs() {
        val sessionString = readSessionFile()
        if (sessionString != null) {
            val parser = LastSessionParser(applicationContext)
            parser.parse(sessionString)
        } else {
            Log.e("QWANT_BROWSER", "restore tabs session file is null")
        }
    }

    override fun onBackPressed() {
        val sessionManager = components.core.sessionManager
        if (sessionManager.selectedSession != null) {
            val url = components.core.sessionManager.selectedSession!!.url
            Log.d("QWANT_BROWSER","back with intent ${sessionManager.selectedSession!!.source.name}")
            if (!sessionManager.selectedSession!!.canGoBack && sessionManager.selectedSession!!.source.name == "ACTION_VIEW") {
                // Tab has been opened from external app, so we close the app to get back to it, after closing the tab
                sessionManager.remove(sessionManager.selectedSession!!)
                this.finish()
                return
            } else if (url.startsWith(getString(R.string.homepage_startwith_filter)) && url.contains("&o=")) {
                // Fix for closing qwant opened medias with back button
                components.useCases.sessionUseCases.loadUrl(url.substringBefore("&o="), flags = EngineSession.LoadUrlFlags.select(64)) // FLAG_REPLACE_HISTORY from GeckoSession native
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
                fragment.setTabsClosedCallback(::fragmentClosed)
            }
            is BookmarksFragment -> {
                if (bookmarksStorage != null) fragment.setBookmarkStorage(bookmarksStorage!!)
                fragment.setBookmarksClosedCallback(::fragmentClosed)
            }
            is HistoryFragment -> {
                fragment.setHistoryClosedCallback(::fragmentClosed)
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
        sessionManager.remove(session)
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

    fun fullScreenChanged(enabled: Boolean) {
        qwantbar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun showTabs() {
        val tag = "TABS_FRAGMENT"
        var tabsFragment = this.supportFragmentManager.findFragmentByTag(tag)
        if (tabsFragment == null) {
            Log.d("QWANT_BROWSER", "showTabs - no tabs fragment available in fragment manager")
            tabsFragment = TabsTrayFragment()
        }
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, tabsFragment, tag)
            // addToBackStack(tag)
            commit()
        }
        /* } else {
            Log.d("QWANT_BROWSER", "showTabs - something else !")
            this.supportFragmentManager.beginTransaction().show(tabsFragment).commit()
        } */
        this.supportFragmentManager.executePendingTransactions()
        /* this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, TabsTrayFragment(), "TABS_FRAGMENT")
            commit()
        } */
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.TABS)
    }

    private fun showBookmarks() {
        // this.replaceFragment(BookmarksFragment)
        var bookmarksFragment = this.supportFragmentManager.findFragmentByTag("BOOKMARKS_FRAGMENT")
        if (bookmarksFragment == null) {
            Log.d("QWANT_BROWSER", "showBookmarks - no bookmarks fragment available in fragment manager")
            bookmarksFragment = BookmarksFragment()
        }
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, bookmarksFragment, "BOOKMARKS_FRAGMENT")
            // addToBackStack("BOOKMARKS_FRAGMENT")
            commit()
        }
        /* } else {
            Log.d("QWANT_BROWSER", "showBookmarks - something else !")
            this.supportFragmentManager.beginTransaction().show(bookmarksFragment).commit()
        } */
        this.supportFragmentManager.executePendingTransactions()
        /* this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, BookmarksFragment(), "BOOKMARKS_FRAGMENT")
            commit()
        } */
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.BOOKMARKS)
    }

    fun showBrowserFragment() {
        var browserFragment = this.supportFragmentManager.findFragmentByTag("BROWSER_FRAGMENT")
        if (browserFragment == null) {
            Log.d("QWANT_BROWSER", "showBrowserFragment - no browser fragment available in fragment manager")
            browserFragment = BrowserFragment.create()
        }

        (browserFragment as BrowserFragment).closeAwesomeBarIfOpen()

        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, browserFragment, "BROWSER_FRAGMENT")
            addToBackStack("BROWSER_FRAGMENT")
            commit()
        }
        /* } else {
            Log.d("QWANT_BROWSER", "showBrowserFragment - something else !")
            this.supportFragmentManager.beginTransaction().show(browserFragment).commit()
        } */
        this.supportFragmentManager.executePendingTransactions()
    }

    fun showHistory() {
        var historyFragment = this.supportFragmentManager.findFragmentByTag("HISTORY_FRAGMENT")
        if (historyFragment == null) {
            Log.d("QWANT_BROWSER", "showHistoryFragment - no browser fragment available in fragment manager")
            historyFragment = HistoryFragment()
        }
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, historyFragment, "HISTORY_FRAGMENT")
            // addToBackStack("HISTORY_FRAGMENT")
            commit()
        }
        this.supportFragmentManager.executePendingTransactions()
    }

    fun showSettings() {
        var settingsFragment = this.supportFragmentManager.findFragmentByTag("SETTINGS_FRAGMENT")
        if (settingsFragment == null) {
            Log.d("QWANT_BROWSER", "showSettings - no settings fragment available in fragment manager")
            settingsFragment = SettingsContainerFragment.create()
        }
        this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, settingsFragment, "SETTINGS_FRAGMENT")
            // addToBackStack("SETTINGS_FRAGMENT")
            commit()
        }
        /* } else {
            Log.d("QWANT_BROWSER", "showSettings - something else !")
            this.supportFragmentManager.beginTransaction().show(settingsFragment).commit()
        } */
        this.supportFragmentManager.executePendingTransactions()
        // this.replaceFragment(SettingsContainerFragment)
        /* this.supportFragmentManager.beginTransaction().apply {
            this.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            replace(R.id.container, SettingsContainerFragment.create(), "SETTINGS_FRAGMENT")
            commit()
        } */
        qwantbarSessionObserver?.setupHomeBar()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.MORE)
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

    private fun fragmentClosed() {
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
        fragmentClosed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        components.core.historyStorage.run { this.persist() }
        this.bookmarksStorage?.persist()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        components.core.historyStorage.run { this.restore() }
        this.bookmarksStorage?.restore()
    }

    private fun replaceFragment(fragment: Fragment) {
        val backStateName = fragment.javaClass.name
        val manager = supportFragmentManager
        val fragmentPopped: Boolean = manager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            val ft = manager.beginTransaction()
            ft.replace(R.id.container, fragment, backStateName)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            // ft.addToBackStack(backStateName)
            ft.commit()
        }
    }

    private fun quitApp() {
        finishAffinity()
        exitProcess(0)
    }

    companion object {
        lateinit var PACKAGE_NAME: String
    }
}
