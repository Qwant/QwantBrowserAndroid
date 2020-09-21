package org.mozilla.reference.browser.layout

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.component_qwantbar.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.BrowserMenuDivider
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.pwa.WebAppUseCases
// import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.browser.FindInPageIntegration
import org.mozilla.reference.browser.browser.QwantBarSessionObserver
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.storage.bookmarks.BookmarksStorage
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
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    private val sessionManagerObserver = object : SessionManager.Observer {
        override fun onSessionAdded(session: Session) { updateTabCount() }
        override fun onSessionRemoved(session: Session) { updateTabCount() }
        override fun onSessionsRestored() { updateTabCount() }
        override fun onAllSessionsRemoved() { updateTabCount() }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_qwantbar, this, true)

        reference = WeakReference(qwantbar_button_tabs)

        sessionManager.register(sessionManagerObserver, view = this)

        tabButtonBox = qwantbar_button_tabs.findViewById(R.id.counter_box)
        tabButtonBar = qwantbar_button_tabs.findViewById(R.id.counter_bar)
        tabButtonText = qwantbar_button_tabs.findViewById(R.id.counter_text)

        val colorDefault = ContextCompat.getColor(context, this.getIconColor(false))
        tabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, colorDefault))
        tabButtonBar?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_bar, colorDefault))
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
        if (session != null && session.url.startsWith(context.getString(R.string.settings_page_startwith_filter))) {
            this.setHighlight(QwantBarSelection.MORE)
        } else if (session == null || session.url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
            this.setHighlight(QwantBarSelection.SEARCH)
        }
    }

    fun setBookmarkStorage(storage: BookmarksStorage) { this.bookmarksStorage = storage }

    fun onTabsClicked(callback: () -> Unit) { tabCallbacks.add(callback) }
    fun onBookmarksClicked(callback: () -> Unit) { bookmarksCallbacks.add(callback) }
    fun onHomeClicked(callback: () -> Unit) { homeCallbacks.add(callback) }
    fun onMenuClicked(callback: () -> Unit) { menuCallbacks.add(callback) }
    fun onBackClicked(callback: () -> Unit) { backCallbacks.add(callback) }
    fun onHistoryClicked(callback: () -> Unit) { historyCallbacks.add(callback) }

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
        reference.get()?.setCountWithAnimation(sessionManager.sessions.size)
    }

    fun updateHomeIcon(mode: QwantBarSessionObserver.QwantBarMode?) {
        if (mode == QwantBarSessionObserver.QwantBarMode.NAVIGATION) {
            qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.HOME, false))
        } else {
            val selected = (sessionManager.selectedSession == null || sessionManager.selectedSession!!.url.startsWith(context.getString(R.string.homepage_startwith_filter)))
            qwantbar_button_home.setImageResource(this.getIcon(QwantBarIcons.SEARCH, selected))
        }
    }
}
