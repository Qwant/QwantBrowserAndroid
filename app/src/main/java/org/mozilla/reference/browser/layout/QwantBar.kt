package org.mozilla.reference.browser.layout

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.component_qwantbar.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.BrowserMenuDivider
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.ContentState
import mozilla.components.browser.state.state.LoadRequestState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.feature.pwa.WebAppUseCases
// import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.lib.state.Action
import mozilla.components.lib.state.Observer
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.lib.state.ext.observe
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.gecko.util.ThreadUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.browser.FindInPageIntegration
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.storage.bookmarks.BookmarksStorage
import java.lang.Runnable
import java.lang.ref.WeakReference

class QwantBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    enum class QwantBarSelection {
        NONE, SEARCH, BOOKMARKS, TABS, MORE
    }

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private val sessionManager: SessionManager = context.applicationContext.application.components.core.sessionManager
    private val sessionUseCases: SessionUseCases = context.applicationContext.application.components.useCases.sessionUseCases
    private val webAppUseCases: WebAppUseCases = context.applicationContext.application.components.useCases.webAppUseCases
    private var bookmarksStorage: BookmarksStorage? = null

    private val tabCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val bookmarksCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val homeCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val menuCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val backCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val historyCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val quitAppCallbacks: MutableList<() -> Unit> = mutableListOf()

    private var tabButtonBox: ImageView? = null
    private var tabButtonBar: ImageView? = null
    private var tabButtonText: TextView? = null

    private var currentPrivacyEnabled = false

    private val menuItems: List<BrowserMenuItem> by lazy {
        listOf(
            BrowserMenuItemToolbar(listOf(
                BrowserMenuItemToolbar.Button(
                    mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
                    iconTintColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    contentDescription = context.getString(R.string.context_menu_refresh)
                ) {
                    sessionUseCases.reload.invoke()
                },
                BrowserMenuItemToolbar.Button(
                    mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
                    iconTintColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    contentDescription = context.getString(R.string.context_menu_stop)
                ) {
                    sessionUseCases.stopLoading.invoke()
                }
            )),

            BrowserMenuDivider(),

            BrowserMenuImageText(
                    context.getString(R.string.context_menu_downloads),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_downloads
            ) {
                context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_add_bookmark),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.ic_add_bookmark
            ) {
                bookmarksStorage?.addBookmark(sessionManager.selectedSession)
            }.apply {
                visible = {
                    if (bookmarksStorage != null && sessionManager.selectedSession != null)
                        !bookmarksStorage!!.contains(sessionManager.selectedSession!!.url)
                    else false
                }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_del_bookmark),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.ic_del_bookmark
            ) {
                bookmarksStorage?.deleteBookmark(sessionManager.selectedSession)
            }.apply {
                visible = {
                    if (bookmarksStorage != null && sessionManager.selectedSession != null)
                        bookmarksStorage!!.contains(sessionManager.selectedSession!!.url)
                    else false
                }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_share),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.ic_share
            ) {
                val url = sessionManager.selectedSession?.url ?: ""
                context.share(url)
            }.apply {
                visible = { sessionManager.selectedSession != null }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_add_homescreen),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.ic_add_homescreen
            ) {
                MainScope().launch { webAppUseCases.addToHomescreen() }
            }.apply {
                visible = { webAppUseCases.isPinningSupported() }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_find),
                textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                imageResource = R.drawable.ic_search
            ) {
                FindInPageIntegration.launch?.invoke()
            }.apply {
                visible = { sessionManager.selectedSession != null }
            },

            BrowserMenuDivider(),

            BrowserMenuImageText(
                    context.getString(R.string.context_menu_addons),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_addons
            ) {
                val intent = Intent(context, AddonsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },

            BrowserMenuImageText(
                    context.getString(R.string.bookmarks),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_bookmark
            ) {
                this.emitOnBookmarksClicked()
            },

            BrowserMenuImageText(
                    context.getString(R.string.history),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_history
            ) {
                this.emitOnHistoryClicked()
            },

            BrowserMenuImageText(
                    context.getString(R.string.settings),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_menu
            ) {
                this.emitOnMenuClicked()
            },

            BrowserMenuImageText(
                    context.getString(R.string.context_menu_close_tab),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_close_tab
            ) {
                if (sessionManager.selectedSession != null) {
                    context.components.useCases.tabsUseCases.removeTab.invoke(sessionManager.selectedSession!!.id)
                }
            },

            BrowserMenuImageText(
                    context.getString(R.string.context_menu_quit_app),
                    textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                    imageResource = R.drawable.ic_quit_app
            ) {
                quitAppCallbacks.forEach { it.invoke() }
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    /* private val sessionManagerObserver = object : SessionManager.Observer {
        override fun onSessionAdded(session: Session) { updateTabCount() }
        override fun onSessionRemoved(session: Session) { updateTabCount() }
        override fun onSessionsRestored() { updateTabCount() }
        override fun onAllSessionsRemoved() { updateTabCount() }
    } */

    /* private val qwantbarSessionObserver = QwantBarSessionObserver(this, sessionManager)
    private val qwantbarBrowserObserver = QwantBarBrowserObserver(this, sessionManager)
    private val qwantbarContentObserver = QwantBarContentObserver(this, sessionManager)
    private val qwantbarLoadRequestObserver = QwantBarLoadRequestObserver(this, sessionManager) */

    init {
        LayoutInflater.from(context).inflate(R.layout.component_qwantbar, this, true)

        // context.components.core.store.observe(qwantbar, qwantbarSessionObserver)
        // context.components.core.store.observe(qwantbar, qwantbarBrowserObserver)
        // context.components.core.store.observe(qwantbar, qwantbarContentObserver)
        // context.components.core.store.observe(qwantbar, qwantbarLoadRequestObserver)

        reference = WeakReference(qwantbar_button_tabs)

        tabButtonBox = qwantbar_button_tabs.findViewById(R.id.counter_box)
        // tabButtonBar = qwantbar_button_tabs.findViewById(R.id.counter_bar)
        tabButtonText = qwantbar_button_tabs.findViewById(R.id.counter_text)

        val colorDefault = ContextCompat.getColor(context, this.getIconColor(false))
        tabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, colorDefault))
        // tabButtonBar?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_bar, colorDefault))
        tabButtonText?.setTextColor(colorDefault)

        qwantbar_button_tabs.setCount(sessionManager.sessions.size)
        qwantbar_layout_tabs.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            this.emitOnTabsClicked()
        }
        qwantbar_button_tabs.contentDescription = context.getString(R.string.mozac_feature_tabs_toolbar_tabs_button)

        qwantbar_layout_home.setOnClickListener { emitOnHomeClicked() }
        qwantbar_layout_bookmarks.setOnClickListener { emitOnBookmarksClicked() }
        qwantbar_layout_menu_qwant.setOnClickListener { emitOnMenuClicked() }
        qwantbar_layout_nav_back.setOnClickListener { backCallbacks.forEach { it.invoke() } } // sessionUseCases.goBack.invoke() }
        qwantbar_layout_nav_forward.setOnClickListener { sessionUseCases.goForward.invoke() }

        qwantbar_button_menu_nav.menuBuilder = menuBuilder
        qwantbar_button_menu_nav.setColorFilter(ContextCompat.getColor(context, this.getIconColor(false)))

        val session = sessionManager.selectedSession
        if (session == null || session.url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
            this.setHighlight(QwantBarSelection.SEARCH)
        }

        this.checkSession()
    }

    fun setBookmarkStorage(storage: BookmarksStorage) { this.bookmarksStorage = storage }

    fun onTabsClicked(callback: () -> Unit) { tabCallbacks.add(callback) }
    fun onBookmarksClicked(callback: () -> Unit) { bookmarksCallbacks.add(callback) }
    fun onHomeClicked(callback: () -> Unit) { homeCallbacks.add(callback) }
    fun onMenuClicked(callback: () -> Unit) { menuCallbacks.add(callback) }
    fun onBackClicked(callback: () -> Unit) { backCallbacks.add(callback) }
    fun onHistoryClicked(callback: () -> Unit) { historyCallbacks.add(callback) }
    fun onQuitAppClicked(callback: () -> Unit) { quitAppCallbacks.add(callback) }

    private var currentSelection: QwantBarSelection = QwantBarSelection.SEARCH

    fun setHighlight(selection: QwantBarSelection) {
        currentSelection = selection

        val colorDefault = ContextCompat.getColor(context, this.getIconColor(false))
        val colorSelected = ContextCompat.getColor(context, this.getIconColor(true))

        qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.SEARCH, (selection == QwantBarSelection.SEARCH)))
        qwantbar_text_home.setTextColor(if (selection == QwantBarSelection.SEARCH) colorSelected else colorDefault)

        qwantbar_button_bookmarks.setImageResource(this.getIcon(QwantBarIcons.BOOKMARKS, (selection == QwantBarSelection.BOOKMARKS)))
        qwantbar_text_bookmarks.setTextColor(if (selection == QwantBarSelection.BOOKMARKS) colorSelected else colorDefault)

        qwantbar_button_menu_qwant.setImageResource(this.getIcon(QwantBarIcons.MENU_QWANT, (selection == QwantBarSelection.MORE)))
        qwantbar_text_menu_qwant.setTextColor(if (selection == QwantBarSelection.MORE) colorSelected else colorDefault)

        val tabColor = if (selection == QwantBarSelection.TABS) colorSelected else colorDefault
        tabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, tabColor))
        tabButtonBar?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_bar, tabColor))
        tabButtonText?.setTextColor(tabColor)
        qwantbar_text_tabs.setTextColor(if (selection == QwantBarSelection.TABS) colorSelected else colorDefault)

        qwantbar_button_menu_nav.setColorFilter(ContextCompat.getColor(context, this.getIconColor(false)))
    }

    fun setPrivacyMode(enabled: Boolean) {
        if (enabled != currentPrivacyEnabled) {
            // qwantbar_container.setBackgroundColor(resources.getColor(if (enabled) R.color.qwantbar_background_privacy else R.color.photonWhite))
            currentPrivacyEnabled = enabled
            this.setHighlight(currentSelection)
        }
    }

    enum class QwantBarIcons {
        SEARCH, HOME, BOOKMARKS, MENU_QWANT
    }

    fun getIcon(icon: QwantBarIcons, selected: Boolean) : Int {
        return when (icon) {
            QwantBarIcons.SEARCH -> {
                if (currentPrivacyEnabled && selected) R.drawable.ic_search_privacy_selected
                else if (currentPrivacyEnabled && !selected) R.drawable.ic_search
                else if (!currentPrivacyEnabled && selected) R.drawable.ic_search_selected
                else R.drawable.ic_search
            }
            QwantBarIcons.HOME -> {
                // Should not have "selected" state as it transforms into a search
                // As it is qwant icon, there is no privacy state neither
                R.drawable.ic_qwant_logo
            }
            QwantBarIcons.BOOKMARKS -> {
                if (currentPrivacyEnabled && selected) R.drawable.ic_bookmark_privacy_selected
                else if (currentPrivacyEnabled && !selected) R.drawable.ic_bookmark
                else if (!currentPrivacyEnabled && selected) R.drawable.ic_bookmark_selected
                else R.drawable.ic_bookmark
            }
            QwantBarIcons.MENU_QWANT -> {
                if (currentPrivacyEnabled && selected) R.drawable.ic_menu_privacy_selected
                else if (currentPrivacyEnabled && !selected) R.drawable.ic_menu
                else if (!currentPrivacyEnabled && selected) R.drawable.ic_menu_selected
                else R.drawable.ic_menu
            }
        }
    }

    private fun getIconColor(selected: Boolean) : Int  {
        return if (currentPrivacyEnabled) {
            if (selected) R.color.menu_items_privacy_selected else R.color.menu_items
        } else {
            if (selected) R.color.menu_items_selected else R.color.menu_items
        }
    }

    private fun emitOnTabsClicked() {
        tabCallbacks.forEach {
            it.invoke()
        }
    }

    private fun emitOnBookmarksClicked() {
        bookmarksCallbacks.forEach {
            it.invoke()
        }
    }

    private fun emitOnHistoryClicked() {
        historyCallbacks.forEach {
            it.invoke()
        }
    }

    private fun emitOnHomeClicked() {
        homeCallbacks.forEach {
            it.invoke()
        }
    }

    private fun emitOnMenuClicked() {
        menuCallbacks.forEach {
            it.invoke()
        }
    }

    fun updateTabCount() {
        /* sessionManager.sessions.size */
        reference.get()?.setCountWithAnimation(context.components.core.store.state.getNormalOrPrivateTabs(currentPrivacyEnabled).size)
    }

    fun updateTabCount(isPrivate: Boolean) {
        /* sessionManager.sessions.size */
        reference.get()?.setCountWithAnimation(context.components.core.store.state.getNormalOrPrivateTabs(isPrivate).size)
    }

    fun updateHomeIcon(mode: QwantBarMode?) {
        if (mode == QwantBarMode.NAVIGATION) {
            qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.HOME, false))
        } else {
            val selected = (sessionManager.selectedSession == null || sessionManager.selectedSession!!.url.startsWith(context.getString(R.string.homepage_startwith_filter)))
            qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.SEARCH, selected))
        }
    }

    private var currentMode: QwantBarMode = QwantBarMode.HOME

    enum class QwantBarMode {
        HOME, NAVIGATION
    }

    fun getCurrentMode() : QwantBarMode {
        return currentMode
    }

    private fun showButtonsTexts() {
        qwantbar_text_home.visibility = View.VISIBLE
        qwantbar_text_bookmarks.visibility = View.VISIBLE
        qwantbar_text_tabs.visibility = View.VISIBLE
        qwantbar_text_menu_qwant.visibility = View.VISIBLE
    }

    private fun hideButtonsTexts() {
        qwantbar_text_home.visibility = View.GONE
        qwantbar_text_bookmarks.visibility = View.GONE
        qwantbar_text_tabs.visibility = View.GONE
        qwantbar_text_menu_qwant.visibility = View.GONE
    }

    private fun setBarHeight(height_dp: Int) {
        val qwantbarParams = this.layoutParams
        if (qwantbarParams != null) {
            qwantbarParams.height = height_dp.dpToPx(Resources.getSystem().displayMetrics)
            this.layoutParams = qwantbarParams
        }
    }

    fun setupHomeBar() {
        this.setBarHeight(56)
        this.showButtonsTexts()
        val selected = (sessionManager.selectedSession == null || sessionManager.selectedSession!!.url.startsWith("https://www.qwant.com"))
        qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.SEARCH, selected))
        if (selected) this.setHighlight(QwantBarSelection.SEARCH)
        // qwantbar.setBookmarkButton(QwantBar.BookmarkButtonType.OPEN)

        qwantbar_layout_nav_back.visibility = View.GONE
        qwantbar_layout_nav_forward.visibility = View.GONE
        qwantbar_layout_bookmarks.visibility = View.VISIBLE
        qwantbar_layout_menu_qwant.visibility = View.VISIBLE
        qwantbar_layout_menu_nav.visibility = View.GONE

        currentMode = QwantBarMode.HOME
    }

    fun setupNavigationBar() {
        this.setBarHeight(36)
        this.hideButtonsTexts()
        qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.HOME, false))

        qwantbar_layout_nav_back.visibility = View.VISIBLE
        qwantbar_layout_nav_forward.visibility = View.VISIBLE
        qwantbar_layout_bookmarks.visibility = View.GONE
        qwantbar_layout_menu_qwant.visibility = View.GONE
        qwantbar_layout_menu_nav.visibility = View.VISIBLE

        currentMode = QwantBarMode.NAVIGATION
    }

    fun checkSession(url: String?) {
        if (url == null || url.startsWith(context.getString(R.string.homepage_base))) { // todo do not include results page
            if (currentMode != QwantBarMode.HOME) {
                ThreadUtils.runOnUiThread(Runnable {
                    setupHomeBar()
                })
            }
        } else {
            if (currentMode != QwantBarMode.NAVIGATION) {
                ThreadUtils.runOnUiThread(Runnable {
                    setupNavigationBar()
                })
            }
        }
    }

    fun changeForwardButton(canForward: Boolean) {
        val forwardColor = if (canForward) context.getColorFromAttr(R.attr.qwant_color_main) else context.getColorFromAttr(R.attr.qwant_color_light)
        qwantbar_button_nav_forward.setBackgroundColor(forwardColor)
    }

    fun changeBackwardButton(canBackward: Boolean) {
        val backwardColor = if (canBackward) context.getColorFromAttr(R.attr.qwant_color_main) else context.getColorFromAttr(R.attr.qwant_color_light)
        qwantbar_button_nav_back.setBackgroundColor(backwardColor)
    }

    fun checkSession(session: Session) {
        checkSession(session.url)
        this.setPrivacyMode(session.private)
        context.setTheme(if (session.private) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
    }

    fun checkSession() {
        if (sessionManager.selectedSession != null) this.checkSession(sessionManager.selectedSession!!)
    }

    /* class QwantBarSessionObserver(
            private val qwantbar: QwantBar,
            private val sessionManager: SessionManager
    ) : Observer<SessionState> {
        override fun invoke(sessionState: SessionState) {
            Log.d("QWANT_BROWSER", "Observer SessionState: session id = ${sessionState.id}")
            if (sessionManager.selectedSession != null && sessionState.id == sessionManager.selectedSession!!.id) {
                qwantbar.checkSession(sessionManager.selectedSession!!)
            }
        }
    }

    class QwantBarBrowserObserver(
            private val qwantbar: QwantBar,
            private val sessionManager: SessionManager
    ) : Observer<BrowserState> {
        override fun invoke(browserState: BrowserState) {
            Log.d("QWANT_BROWSER", "Observer BrowserState: tab count = ${browserState.tabs.size}")
            Log.d("QWANT_BROWSER", "Observer BrowserState: tab count with privacy filter = ${browserState.tabs.filter { it.content.private == (browserState.selectedTab?.content?.private ?: true) } }")
            Log.d("QWANT_BROWSER", "Observer BrowserState: selected tab id = ${browserState.selectedTabId}")
            Log.d("QWANT_BROWSER", "Observer BrowserState: selected tab privacy = ${browserState.selectedTab?.content?.private ?: "no selected tab"}")
        }
    }

    class QwantBarContentObserver(
            private val qwantbar: QwantBar,
            private val sessionManager: SessionManager
    ) : Observer<ContentState> {
        override fun invoke(contentState: ContentState) {
            Log.d("QWANT_BROWSER", "Observer Content: url = ${contentState.url}")
            Log.d("QWANT_BROWSER", "Observer Content: private = ${contentState.private}")
            Log.d("QWANT_BROWSER", "Observer Content: cangoback = ${contentState.canGoBack}")
            Log.d("QWANT_BROWSER", "Observer Content: cangoforward = ${contentState.canGoForward}")
            Log.d("QWANT_BROWSER", "Observer Content: fullscreen = ${contentState.fullScreen}")
        }
    }

    class QwantBarLoadRequestObserver(
            private val qwantbar: QwantBar,
            private val sessionManager: SessionManager
    ) : Observer<LoadRequestState> {
        override fun invoke(loadRequestState: LoadRequestState) {
            Log.d("QWANT_BROWSER", "Observer LoadRequest: url = ${loadRequestState.url}")
        }
    } */
}
