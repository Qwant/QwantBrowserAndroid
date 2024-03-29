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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.qwant.android.onboarding.OnboardingActivity
import com.qwant.android.webext.ABPRemovalActivity
import kotlinx.android.synthetic.main.activity_main.*
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.utils.SafeIntent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.layout.QwantBar
import org.mozilla.reference.browser.layout.QwantBarFeature
import org.mozilla.reference.browser.settings.SettingsContainerFragment
import org.mozilla.reference.browser.storage.bookmarks.BookmarksFragment
import org.mozilla.reference.browser.storage.bookmarks.BookmarksStorage
import org.mozilla.reference.browser.storage.history.HistoryFragment
// import org.mozilla.reference.browser.tabs.QwantTabsFragment
import java.util.*
import kotlin.system.exitProcess

import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.qwant.android.compose.tabs.TabsFragment

/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : AppCompatActivity(), SettingsContainerFragment.OnSettingsClosed {
    private var bookmarksStorage: BookmarksStorage? = null
    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    private val tab: SessionState?
        get() = components.core.store.state.findCustomTabOrSelectedTab(sessionId)

    private var darkmode: Int = 0

    private val qwantBarFeature = ViewBoundFeatureWrapper<QwantBarFeature>()

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?): Fragment =
            BrowserFragment.create(sessionId)

    override fun onCreate(savedInstanceState: Bundle?) {
        PACKAGE_NAME = packageName

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        if (intent.action == "CLOSE_APP") { // should be called with flag Intent.FLAG_ACTIVITY_NEW_TASK
            this.quitApp()
            return
        }

        this.loadLocale()

        Log.d("QWANT_BROWSER_T", "load browser activity")

        Log.e("QWANT_BROWSER_BT", "client string from buildType: " + getString(R.string.app_client_string))

        darkmode = if (intent.hasExtra("newTheme")) {
            Log.d("QWANT_BROWSER_T", "with theme " + intent.getIntExtra("newTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
            intent.getIntExtra("newTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            Log.d("QWANT_BROWSER_T", "with theme default " + (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK))
            applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }

        Log.d("QWANT_BROWSER_T", "darkmode: $darkmode")

        this.ratePushCheck()

        val statusbarBackground = getColorFromAttr(R.attr.qwant_systembar_background)
        window?.statusBarColor = statusbarBackground
        window?.navigationBarColor = statusbarBackground
        val view = window?.decorView
        if (window != null && view != null) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.isAppearanceLightNavigationBars = (darkmode == 16)
            controller.isAppearanceLightStatusBars = (darkmode == 16)
        }

        bookmarksStorage = BookmarksStorage(applicationContext)
        bookmarksStorage?.restore()

        qwantBarFeature.set(
            feature = QwantBarFeature(components.core.store, qwantbar),
            owner = this,
            view = qwantbar
        )

        qwantbar.bookmarksStorage = bookmarksStorage
        qwantbar.onTabsClicked = ::checkRatePushBeforeTabs
        qwantbar.onBookmarksClicked = ::showBookmarks
        qwantbar.onHomeClicked = ::showHome
        qwantbar.onSettingsClicked = ::showSettings
        qwantbar.onBackClicked = ::onBackPressed
        qwantbar.onHistoryClicked = ::showHistory
        qwantbar.onQuitAppClicked = ::quitApp

        if (savedInstanceState == null) {
            when (intent.action) {
                "CHANGED_LANGUAGE" -> {
                    qwantbar.setHighlight(QwantBar.QwantBarSelection.SETTINGS)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, SettingsContainerFragment.create(language_changed_reload = true, theme_changed_reload = false), "SETTINGS_FRAGMENT")
                        commit()
                    }
                    intent.action = null
                }
                "CHANGED_THEME" -> {
                    qwantbar.setHighlight(QwantBar.QwantBarSelection.SETTINGS)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, SettingsContainerFragment.create(language_changed_reload = false, theme_changed_reload = true), "SETTINGS_FRAGMENT")
                        commit()
                    }
                    intent.action = null
                }
                else -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.container, createBrowserFragment(sessionId), "BROWSER_FRAGMENT")
                        commit()
                    }
                }
            }
        }

        KeyboardVisibilityEvent.setEventListener(this) { visible ->
            qwantbar.visibility = if (visible) View.GONE else View.VISIBLE
        }

        qwantbar.updateTabCount()

        this.removeABP()
        this.checkFirstLaunch()
        this.launchOnboardingIfNecessary()
    }

    private fun ratePushCheck() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val ratepushFirstOpenedKey = getString(R.string.pref_key_ratepush_first_opened_date)
        if (prefs.getLong(ratepushFirstOpenedKey, 0) == 0L) {
            prefs.edit().putLong(ratepushFirstOpenedKey, Calendar.getInstance().time.time).apply()
        }

        if (prefs.getLong(getString(R.string.pref_key_ratepush_date), 0) == 0L) {
            val ratepushOpenedCountKey = getString(R.string.pref_key_ratepush_app_opened_count)
            val ratepushOpenedCount = prefs.getInt(ratepushOpenedCountKey, 0)
            val ratepushConsecutiveDaysKey = getString(R.string.pref_key_ratepush_consecutive_days)
            val ratepushConsecutiveDays = prefs.getInt(ratepushConsecutiveDaysKey, 0)
            val ratepushLastOpenedKey = getString(R.string.pref_key_ratepush_last_opened)
            val editor = prefs.edit()

            if (ratepushConsecutiveDays < RATEPUSH_CONSECUTIVE_DAYS_LIMIT) {
                val now = Calendar.getInstance()

                // now.day
                val last = Calendar.getInstance()
                last.time = Date(prefs.getLong(ratepushLastOpenedKey, 0))

                val nowYear = now.get(Calendar.YEAR)
                val nowDay = now.get(Calendar.DAY_OF_YEAR)
                val lastYear = last.get(Calendar.YEAR)
                val lastDay = last.get(Calendar.DAY_OF_YEAR)
                var increment = false
                var reset = false
                if (nowYear == lastYear) {
                    if (nowDay == lastDay + 1) {
                        increment = true
                    } else if (nowDay != lastDay) {
                        reset = true
                    }
                } else if (now.get(Calendar.YEAR) == last.get(Calendar.YEAR) + 1 && now.get(Calendar.DAY_OF_YEAR) == 1 && last.get(Calendar.DAY_OF_YEAR) == 364) {
                    increment = true
                } else {
                    reset = true
                }

                if (increment) {
                    Log.d("QWANT_BROWSER_RATING", "New consecutive day recorded | total: ${ratepushConsecutiveDays + 1}")
                    editor.putInt(ratepushConsecutiveDaysKey, ratepushConsecutiveDays + 1)
                } else if (reset) {
                    Log.d("QWANT_BROWSER_RATING", "Consecutive days reset to 1")
                    editor.putInt(ratepushConsecutiveDaysKey, 1)
                }

                if (ratepushOpenedCount < RATEPUSH_MIN_CONNECTION) {
                    if (increment || reset) { // new day connection, reset to 1
                        Log.d("QWANT_BROWSER_RATING", "App opened reset to 1")
                        editor.putInt(ratepushOpenedCountKey, 1)
                    } else { // same day connection, increment counter
                        Log.d("QWANT_BROWSER_RATING", "New app opened recorded | total: ${ratepushOpenedCount + 1}")
                        editor.putInt(ratepushOpenedCountKey, ratepushOpenedCount + 1)
                    }
                }

                editor.putLong(ratepushLastOpenedKey, now.time.time)

                editor.apply()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.showBrowserFragment()
    }

    fun removeABP() {
        components.core.engine.listInstalledWebExtensions({ list ->
            list.forEach { ext ->
                if (ext.id == "{d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}") { // adblockplus ID
                    Log.i("QWANT_BROWSER_EXTENSION", "ABP Extension found: deleting")
                    components.core.engine.uninstallWebExtension(ext,
                        onSuccess = {
                            val intent = Intent(this, ABPRemovalActivity::class.java)
                            // intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                            startActivity(intent)
                        },
                        onError = { _, throwable ->
                            Log.e("QWANT_BROWSER_EXTENSION", "Error uninstalling ABP", throwable)
                        }
                    )
                }
            }
        })
    }

    fun launchOnboardingIfNecessary() {
        val prefkey = resources.getString(R.string.pref_key_show_onboarding)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val shouldShowOnboarding = prefs.getBoolean(prefkey, false)
        if (shouldShowOnboarding) {
            val i = Intent(this, OnboardingActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(i)
        }
    }

    private fun checkFirstLaunch() {
        val prefkeyFirstLaunch = resources.getString(R.string.pref_key_first_launch)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val prefEditor: SharedPreferences.Editor = prefs.edit()
        val firstLaunch = prefs.getBoolean(prefkeyFirstLaunch, true)
        if (firstLaunch) {
            prefEditor.putBoolean(resources.getString(R.string.pref_key_show_onboarding), true)
            prefEditor.putBoolean(prefkeyFirstLaunch, false)
            components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(applicationContext))
        }
        prefEditor.apply()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("QWANT_BROWSER_T", "config changed")
        if (darkmode != (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
            val intent = Intent(applicationContext, BrowserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.action = "CHANGED_THEME"
            intent.putExtra("newTheme", newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK)
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

    override fun onPause() {
        super.onPause()
        try {
            val view = this.window.currentFocus
            if (view != null) {
                val wt = view.windowToken
                if (wt != null) {
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(wt, 0)
                }
            }
        } catch (e: Exception) {
            Log.e("QWANT_BROWSER", "fail closing keyboard: ${e.message}")
        }
    }

    override fun onBackPressed() {
        val selectedTab = components.core.store.state.selectedTab
        if (selectedTab != null) {
            val url = selectedTab.content.url
            val canGoBack = components.core.store.state.selectedTab?.content?.canGoBack ?: false
            if (!canGoBack && selectedTab.source.equals("ACTION_VIEW")) {
                // Tab has been opened from external app, so we close the app to get back to it, after closing the tab
                components.useCases.tabsUseCases.removeTab(selectedTab.id)
                this.finish()
                return
            } else if (url.startsWith(getString(R.string.homepage_startwith_filter)) && url.contains("&o=")) {
                // Fix for closing qwant opened medias with back button
                components.useCases.sessionUseCases.loadUrl(url.substringBefore("&o="), flags = EngineSession.LoadUrlFlags.select(64)) // FLAG_REPLACE_HISTORY from GeckoSession native
                return
            } else if (url.startsWith(getString(R.string.settings_page_startwith_filter))) {
                // Highlight search icon when on the settings page. Do not return.
                qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            }
        }

        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            }
        }

        removeSessionIfNeeded()
        this.finish()
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is SettingsContainerFragment -> {
                fragment.setOnSettingsClosed(this)
            }
            is TabsFragment -> {
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
    private fun removeSessionIfNeeded(): Boolean {
        val session = tab ?: return false

        return if (session.source is SessionState.Source.External && !session.restored) {
            finish()
            components.useCases.tabsUseCases.removeTab(session.id)
            true
        } else {
            val hasParentSession = session is TabSessionState && session.parentId != null
            if (hasParentSession) {
                components.useCases.tabsUseCases.removeTab(session.id, selectParentIfExists = true)
            }
            // We want to return to home if this session didn't have a parent session to select.
            val goToOverview = !hasParentSession
            !goToOverview
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
            else -> super.onCreateView(parent, name, context, attrs)
        }

    fun fullScreenChanged(enabled: Boolean) {
        qwantbar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun checkRatePushBeforeTabs() {
        Log.d("QWANT_BROWSER_RATING", "Check rate push before tabs")

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val ratePushDate = prefs.getLong(getString(R.string.pref_key_ratepush_date), 0)
        if (ratePushDate == 0L) {
            Log.d("QWANT_BROWSER_RATING", "rating not done yet")
            val ratepushOpenedCount = prefs.getInt(getString(R.string.pref_key_ratepush_app_opened_count), 0)
            val ratepushConsecutiveDays = prefs.getInt(getString(R.string.pref_key_ratepush_consecutive_days), 0)

            val now = Calendar.getInstance().time.time
            val ratepushFirstOpened = prefs.getLong(getString(R.string.pref_key_ratepush_first_opened_date), 0L)
            val ratePushDaysSinceFirstOpened = (now - ratepushFirstOpened) * (1000 * 60 * 60 * 24) // ms to days

            // val searchesCount = prefs.getStringSet(getString(R.string.pref_key_ratepush_searches), mutableSetOf())?.size ?: 0
            if (ratepushOpenedCount >= RATEPUSH_MIN_CONNECTION
                && ratepushConsecutiveDays >= RATEPUSH_CONSECUTIVE_DAYS_LIMIT
                // && searchesCount >= RATEPUSH_SEARCH_LIMIT
                && ratePushDaysSinceFirstOpened > RATEPUSH_DAYS_SINCE_FIRST_LIMIT
            ) {
                Log.d("QWANT_BROWSER_RATING", "Rating conditions OK. Launching rating API.")

                val manager = ReviewManagerFactory.create(this)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(this, reviewInfo)
                        flow.addOnCompleteListener {
                            Log.d("QWANT_BROWSER_RATING", "Rate push DONE")

                            val editor = prefs.edit()
                            editor.putLong(getString(R.string.pref_key_ratepush_date), Date().time)
                            editor.apply()

                            showTabs()
                        }
                    } else {
                        Log.d("QWANT_BROWSER_RATING", "Error requesting review API: ${task.exception}")
                        Log.e("QWANT_BROWSER", "Error requesting review API")
                        showTabs()
                    }
                }
            } else {
                Log.d("QWANT_BROWSER_RATING", "rating conditions not met")
                Log.d("QWANT_BROWSER_RATING", "same day opened count: $ratepushOpenedCount - consecutive days: $ratepushConsecutiveDays - since first day: $ratePushDaysSinceFirstOpened")
                showTabs()
            }
        } else {
            Log.d("QWANT_BROWSER_RATING", "rating already done")
            showTabs()
        }
    }

    private fun showTabs() {
        val tag = "TABS_FRAGMENT"
        var tabsFragment = this.supportFragmentManager.findFragmentByTag(tag)
        if (tabsFragment == null) {
            tabsFragment = TabsFragment() // TabsTrayFragment()
        }

        // (tabsFragment as QwantTabsFragment).setPrivacy(components.core.store.state.selectedTab?.content?.private ?: false)

        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, tabsFragment, tag)
            commit()
        }
        this.supportFragmentManager.executePendingTransactions()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.TABS)
        qwantbar.hideIfInNavigation()
    }

    private fun showBookmarks() {
        val isPrivate = components.core.store.state.selectedTab?.content?.private ?: false
        setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)

        var bookmarksFragment = this.supportFragmentManager.findFragmentByTag("BOOKMARKS_FRAGMENT")
        if (bookmarksFragment == null) {
            bookmarksFragment = BookmarksFragment()
        }
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, bookmarksFragment, "BOOKMARKS_FRAGMENT")
            commit()
        }
        this.supportFragmentManager.executePendingTransactions()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.BOOKMARKS)
        qwantbar.setPrivacyModeFromBrowser()
        qwantbar.hideIfInNavigation()
    }

    fun showBrowserFragment() {
        if (applicationContext.components.core.store.state.tabs.isEmpty()) {
            applicationContext.components.useCases.tabsUseCases.addTab(QwantUtils.getHomepage(applicationContext))
        }

        val isPrivate = components.core.store.state.selectedTab?.content?.private ?: false
        setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)

        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, BrowserFragment.create(), "BROWSER_FRAGMENT")
            addToBackStack("BROWSER_FRAGMENT")
            commit()
        }

        this.supportFragmentManager.executePendingTransactions()

        qwantbar.setPrivacyModeFromBrowser()
        qwantbar.visibility = View.VISIBLE
    }

    fun showHistory() {
        val isPrivate = components.core.store.state.selectedTab?.content?.private ?: false
        setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)

        var historyFragment = this.supportFragmentManager.findFragmentByTag("HISTORY_FRAGMENT")
        if (historyFragment == null) {
            historyFragment = HistoryFragment()
        }
        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, historyFragment, "HISTORY_FRAGMENT")
            commit()
        }
        this.supportFragmentManager.executePendingTransactions()
        qwantbar.setPrivacyModeFromBrowser()
        qwantbar.hideIfInNavigation()
    }

    private fun showSettings() {
        val isPrivate = components.core.store.state.selectedTab?.content?.private ?: false
        setTheme(if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)

        this.supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, SettingsContainerFragment.create(), "SETTINGS_FRAGMENT")
            commit()
        }
        this.supportFragmentManager.executePendingTransactions()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.SETTINGS)
        qwantbar.setPrivacyModeFromBrowser()
        qwantbar.hideIfInNavigation()
    }

    private fun showHome() {
        val tabId = components.useCases.tabsUseCases.selectOrAddTab(QwantUtils.getHomepage(applicationContext), components.core.store.state.selectedTab?.content?.private ?: false)
        components.useCases.sessionUseCases.reload(tabId)

        this.showBrowserFragment()
        qwantbar.setupHomeBar()
        qwantbar.setPrivacyModeFromBrowser()
        qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
    }

    private fun fragmentClosed() {
        val url = components.core.store.state.selectedTab?.content?.url
        if (url == null || url.startsWith(baseContext.getString(R.string.homepage_base))) {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.SEARCH)
            qwantbar.setupHomeBar()
        } else {
            qwantbar.setHighlight(QwantBar.QwantBarSelection.NONE)
            qwantbar.setupNavigationBar()
        }
        qwantbar.visibility = View.VISIBLE
    }

    override fun settingsClosed() {
        fragmentClosed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        components.core.historyStorage.run { this.persist() }
        this.bookmarksStorage?.persist()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.info("Activity onActivityResult received with " +
                "requestCode: $requestCode, resultCode: $resultCode, data: $data")

        supportFragmentManager.fragments.forEach {
            if (it is ActivityResultHandler && it.onActivityResult(requestCode, data, resultCode)) {
                return
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun quitApp() {
        QwantUtils.clearDataOnQuit(this,
            success = {
                Toast.makeText(this, R.string.cleardata_done, Toast.LENGTH_LONG).show()
                this.trueQuit()
            },
            error = {
                if (it.message == "disabled") {
                    this.trueQuit()
                } else {
                    Toast.makeText(this, R.string.cleardata_failed, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun trueQuit() {
        finishAffinity()
        exitProcess(0)
    }

    companion object {
        lateinit var PACKAGE_NAME: String

        // const val RATEPUSH_SEARCH_LIMIT = 3
        const val RATEPUSH_CONSECUTIVE_DAYS_LIMIT = 3
        const val RATEPUSH_MIN_CONNECTION = 10
        const val RATEPUSH_DAYS_SINCE_FIRST_LIMIT = 3
    }
}
