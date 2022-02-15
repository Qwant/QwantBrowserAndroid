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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.component_qwantbar.view.*
import kotlinx.coroutines.*
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.*
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.gecko.util.ThreadUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.FindInPageIntegration
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.storage.bookmarks.BookmarksStorage
import java.lang.ref.WeakReference

class QwantBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    enum class QwantBarSelection {
        NONE, SEARCH, BOOKMARKS, TABS, SETTINGS
    }
    enum class QwantBarMode {
        HOME, NAVIGATION
    }
    enum class QwantBarIcon {
        SEARCH, BOOKMARKS, SETTINGS
    }

    private val sessionUseCases: SessionUseCases = context.applicationContext.application.components.useCases.sessionUseCases
    private val webAppUseCases: WebAppUseCases = context.applicationContext.application.components.useCases.webAppUseCases

    var bookmarksStorage: BookmarksStorage? = null

    var onHomeClicked: (() -> Unit)? = null
    var onBookmarksClicked: (() -> Unit)? = null
    var onTabsClicked: (() -> Unit)? = null
    var onSettingsClicked: (() -> Unit)? = null
    var onHistoryClicked: (() -> Unit)? = null
    var onBackClicked: (() -> Unit)? = null
    var onQuitAppClicked: (() -> Unit)? = null

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private var referenceNav: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private var tabButtonBox: ImageView? = null
    private var tabButtonText: TextView? = null
    private var navTabButtonBox: ImageView? = null
    private var navTabButtonText: TextView? = null

    private var currentMode: QwantBarMode = QwantBarMode.HOME
    private var currentPrivacyEnabled = false

    private val menuItemColor = R.color.qwant_text
    private val menuItems: List<BrowserMenuItem> by lazy {
        val store = context.components.core.store
        listOf(
            BrowserMenuImageText(
                context.getString(R.string.context_menu_add_bookmark),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_add_bookmark
            ) { bookmarksStorage?.addBookmark(store.state.selectedTab) }
            .apply {
                visible = {
                    if (bookmarksStorage != null && store.state.selectedTab != null)
                        !bookmarksStorage!!.contains(store.state.selectedTab!!.content.url)
                    else false
                }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_del_bookmark),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_del_bookmark
            ) { bookmarksStorage?.deleteBookmark(store.state.selectedTab) }
            .apply {
                visible = {
                    if (bookmarksStorage != null && store.state.selectedTab != null)
                        bookmarksStorage!!.contains(store.state.selectedTab!!.content.url)
                    else false
                }
            },

            BrowserMenuImageText(
                context.getString(R.string.context_menu_share),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_share
            ) {
                val url = store.state.selectedTab?.content?.url ?: ""
                context.share(url)
            }
            .apply { visible = { store.state.selectedTab != null }},

            BrowserMenuImageText(
                context.getString(R.string.context_menu_add_homescreen),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_add_homescreen
            ) { MainScope().launch { webAppUseCases.addToHomescreen() }}
            .apply { visible = { webAppUseCases.isPinningSupported() }},

            BrowserMenuImageSwitch(
                label = context.getString(R.string.context_menu_request_desktop),
                imageResource = R.drawable.icons_device_computer_line,
                initialState = { store.state.selectedTab?.content?.desktopMode == true }
            ) { checked -> sessionUseCases.requestDesktopSite.invoke(checked) }
            .apply { visible = { store.state.selectedTab != null }},

            BrowserMenuImageText(
                context.getString(R.string.context_menu_find),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_search
            ) { FindInPageIntegration.launch?.invoke() }
            .apply { visible = { store.state.selectedTab != null }},

            BrowserMenuDivider(),

            BrowserMenuImageText(
                context.getString(R.string.context_menu_downloads),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_downloads
            ) { context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) },

            BrowserMenuImageText(
                context.getString(R.string.bookmarks),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_bookmark
            ) { onBookmarksClicked?.invoke() },

            BrowserMenuImageText(
                context.getString(R.string.history),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.ic_history
            ) { onHistoryClicked?.invoke() },

            BrowserMenuImageText(
                context.getString(R.string.settings),
                textColorResource = menuItemColor,
                iconTintColorResource = menuItemColor,
                imageResource = R.drawable.icons_system_settings_line
            ) { onSettingsClicked?.invoke() },
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    init {
        LayoutInflater.from(context).inflate(R.layout.component_qwantbar, this, true)

        val colorDefault = context.getColorFromAttr(R.attr.qwantbar_normalColor)

        val isPrivateTab = context.components.core.store.state.selectedTab?.content?.private ?: false
        val tabsCount = context.components.core.store.state.getNormalOrPrivateTabs(isPrivateTab).size

        // Tabs buttons
        // TODO change tabs buttons
        reference = WeakReference(qwantbar_button_tabs)
        tabButtonBox = qwantbar_button_tabs.findViewById(R.id.counter_box)
        tabButtonText = qwantbar_button_tabs.findViewById(R.id.counter_text)
        tabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.qwant_tabcounter_box, colorDefault))
        tabButtonText?.setTextColor(colorDefault)
        qwantbar_button_tabs.setCount(tabsCount)

        referenceNav = WeakReference(qwantbar_button_nav_tabs)
        navTabButtonBox = qwantbar_button_nav_tabs.findViewById(R.id.counter_box)
        navTabButtonText = qwantbar_button_nav_tabs.findViewById(R.id.counter_text)
        navTabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.qwant_tabcounter_box, colorDefault))
        navTabButtonText?.setTextColor(colorDefault)
        qwantbar_button_tabs.setCount(tabsCount)

        // Menu bar
        qwantbar_text_home.setOnClickListener { onHomeClicked?.invoke() }
        qwantbar_text_bookmarks.setOnClickListener { onBookmarksClicked?.invoke() }
        qwantbar_text_settings.setOnClickListener { onSettingsClicked?.invoke() }
        qwantbar_layout_tabs.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onTabsClicked?.invoke()
        }

        // Nav bar
        qwantbar_button_nav_back.setOnClickListener { onBackClicked?.invoke() } // sessionUseCases.goBack.invoke() }
        qwantbar_button_nav_forward.setOnClickListener { sessionUseCases.goForward.invoke() }
        qwantbar_button_nav_home.setOnClickListener { onHomeClicked?.invoke() }
        qwantbar_layout_nav_tabs.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onTabsClicked?.invoke()
        }
        qwantbar_button_nav_menu.menuBuilder = menuBuilder
        qwantbar_button_nav_menu.setColorFilter(colorDefault)

        this.setHighlight(QwantBarSelection.SEARCH)
        this.checkSession()
    }

    private var currentSelection: QwantBarSelection = QwantBarSelection.SEARCH

    fun setHighlight(selection: QwantBarSelection) {
        currentSelection = selection

        val colorBackground = context.getColorFromAttr(R.attr.qwantbar_backgroundColor)
        qwantbar_container.setBackgroundColor(colorBackground)

        val colorDefault = context.getColorFromAttr(R.attr.qwantbar_normalColor)
        val colorSelected = context.getColorFromAttr(R.attr.qwantbar_selectedColor)
        // val background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)

        val colorHome = if (selection == QwantBarSelection.SEARCH) colorSelected else colorDefault
        val drawableHome = DrawableUtils.loadAndTintDrawable(context, this.getIcon(QwantBarIcon.SEARCH), colorHome)
        qwantbar_text_home.setTextColor(colorHome)
        qwantbar_text_home.setCompoundDrawablesWithIntrinsicBounds(null, drawableHome, null, null)
        qwantbar_text_home.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)

        val colorBookmark = if (selection == QwantBarSelection.BOOKMARKS) colorSelected else colorDefault
        val drawableBookmark = DrawableUtils.loadAndTintDrawable(context, this.getIcon(QwantBarIcon.BOOKMARKS), colorBookmark)
        qwantbar_text_bookmarks.setTextColor(colorBookmark)
        qwantbar_text_bookmarks.setCompoundDrawablesWithIntrinsicBounds(null, drawableBookmark, null, null)
        qwantbar_text_bookmarks.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)

        val colorSettings = if (selection == QwantBarSelection.SETTINGS) colorSelected else colorDefault
        val drawableSettings = DrawableUtils.loadAndTintDrawable(context, this.getIcon(QwantBarIcon.SETTINGS), colorSettings)
        qwantbar_text_settings.setTextColor(colorSettings)
        qwantbar_text_settings.setCompoundDrawablesWithIntrinsicBounds(null, drawableSettings, null, null)
        qwantbar_text_settings.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)

        val boxTextColor = if (selection == QwantBarSelection.TABS) ContextCompat.getColor(context, R.color.qwant_white) else colorDefault
        val boxDrawableId = if (selection == QwantBarSelection.TABS) R.drawable.qwant_tabcounter_box_selected else R.drawable.qwant_tabcounter_box
        val boxDrawable = AppCompatResources.getDrawable(context, boxDrawableId)
        tabButtonBox?.setImageDrawable(boxDrawable)
        tabButtonText?.setTextColor(boxTextColor)
        qwantbar_text_tabs.setTextColor(if (selection == QwantBarSelection.TABS) colorSelected else colorDefault)
        navTabButtonBox?.setImageDrawable(boxDrawable)
        navTabButtonText?.setTextColor(boxTextColor)
        qwantbar_layout_tabs.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)

        qwantbar_button_nav_menu.setColorFilter(colorDefault)

        qwantbar_button_nav_back.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)
        qwantbar_button_nav_forward.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)
        qwantbar_button_nav_home.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)
        qwantbar_layout_nav_tabs.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)
        qwantbar_button_nav_menu.background = ContextCompat.getDrawable(context, R.drawable.ripple_qwantbar)
    }

    fun setPrivacyMode(enabled: Boolean) {
        if (enabled != currentPrivacyEnabled) {
            currentPrivacyEnabled = enabled
            qwantbar_button_tabs_privacy.visibility = if (enabled) VISIBLE else GONE
            qwantbar_button_nav_tabs_privacy.visibility = if (enabled) VISIBLE else GONE
            this.setHighlight(currentSelection)
            this.updateTabCount()
        }
    }

    fun setPrivacyModeFromBrowser() {
        this.setPrivacyMode(context.components.core.store.state.selectedTab?.content?.private ?: false)
    }

    private fun getIcon(icon: QwantBarIcon) : Int {
        return when (icon) {
            QwantBarIcon.SEARCH -> {
                if (currentSelection == QwantBarSelection.SEARCH) R.drawable.icons_system_search_fill
                else R.drawable.icons_system_search_line
            }
            QwantBarIcon.BOOKMARKS -> {
                if (currentSelection == QwantBarSelection.BOOKMARKS) R.drawable.icons_business_bookmark_fill
                else R.drawable.icons_business_bookmark_line
            }
            QwantBarIcon.SETTINGS -> {
                if (currentSelection == QwantBarSelection.SETTINGS) R.drawable.icons_system_settings_fill
                else R.drawable.icons_system_settings_line
            }
        }
    }

    fun hideIfInNavigation() {
        if (currentMode == QwantBarMode.NAVIGATION) {
            this.visibility = View.GONE
        }
    }

    fun updateTabCount() {
        this.updateTabCount(currentPrivacyEnabled)
    }

    fun updateTabCount(isPrivate: Boolean) {
        val size = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate).size
        reference.get()?.setCountWithAnimation(size)
        referenceNav.get()?.setCountWithAnimation(size)
    }

    private fun setBarHeight(height_dp: Int) {
        val qwantbarParams = this.layoutParams
        if (qwantbarParams != null) {
            qwantbarParams.height = height_dp.dpToPx(Resources.getSystem().displayMetrics)
            this.layoutParams = qwantbarParams
        }
    }

    fun setupHomeBar() {
        currentMode = QwantBarMode.HOME
        qwantbar_layout_menubar.visibility = View.VISIBLE
        qwantbar_layout_navbar.visibility = View.GONE
        this.setBarHeight(58)

        if (context.components.core.store.state.selectedTab?.content?.url?.startsWith("https://www.qwant.com") == true)
            this.setHighlight(QwantBarSelection.SEARCH)
    }

    fun setupNavigationBar() {
        currentMode = QwantBarMode.NAVIGATION
        qwantbar_layout_menubar.visibility = View.GONE
        qwantbar_layout_navbar.visibility = View.VISIBLE
        this.setBarHeight(48)
    }

    fun changeForwardButton(canForward: Boolean) {
        val forwardColor = if (canForward) context.getColorFromAttr(R.attr.qwantbar_normalColor) else context.getColorFromAttr(R.attr.qwantbar_disabledColor)
        qwantbar_button_nav_forward.setColorFilter(forwardColor)
    }

    fun changeBackwardButton(canBackward: Boolean) {
        qwantbar_button_nav_back.isEnabled = canBackward
        val backwardColor = if (canBackward) context.getColorFromAttr(R.attr.qwantbar_normalColor) else context.getColorFromAttr(R.attr.qwantbar_disabledColor)
        qwantbar_button_nav_back.setColorFilter(backwardColor)
    }

    fun checkSession(url: String?) {
        if (url == null || url.startsWith(context.getString(R.string.homepage_base))) { // todo do not include results page ??
            if (currentMode != QwantBarMode.HOME) {
                ThreadUtils.runOnUiThread {
                    setupHomeBar()
                }
            }
        } else {
            if (currentMode != QwantBarMode.NAVIGATION) {
                ThreadUtils.runOnUiThread {
                    setupNavigationBar()
                }
            }
        }
    }

    private fun checkSession(session: TabSessionState?) {
        checkSession(session?.content?.url)
        val private = session?.content?.private ?: false
        Log.d("QWANT_BROWSER", "Set privacy from checkSession")
        this.setPrivacyMode(private)
        context.setTheme(if (private) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar)
    }

    private fun checkSession() {
        this.checkSession(context.components.core.store.state.selectedTab)
    }
}
