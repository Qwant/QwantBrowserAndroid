package org.mozilla.reference.browser.layout

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.component_qwantbar.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.BrowserMenuSwitch
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.utils.DrawableUtils
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.browser.FindInPageIntegration
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity
import java.lang.ref.WeakReference

class QwantBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    enum class QwantBarSelection {
        NONE, SEARCH, BOOKMARKS, TABS
    }

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private val sessionManager: SessionManager = context.applicationContext.application.components.core.sessionManager
    private val sessionUseCases: SessionUseCases = context.applicationContext.application.components.useCases.sessionUseCases
    private val webAppUseCases: WebAppUseCases = context.applicationContext.application.components.useCases.webAppUseCases

    private val tabCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val bookmarksCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val homeCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val backCallbacks: MutableList<() -> Unit> = mutableListOf()

    private var tabButtonBox: ImageView? = null
    private var tabButtonBar: ImageView? = null
    private var tabButtonText: TextView? = null

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = R.color.icons,
            contentDescription = "Forward",
            isEnabled = { sessionManager.selectedSession?.canGoForward == true }) {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = R.color.icons,
            contentDescription = "Refresh") {
            sessionUseCases.reload.invoke()
        }

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = R.color.icons,
            contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke()
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    private val menuItems: List<BrowserMenuItem> by lazy {
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem("Share") {
                val url = sessionManager.selectedSession?.url ?: ""
                context.share(url)
            }.apply {
                visible = { sessionManager.selectedSession != null }
            },

            BrowserMenuSwitch("Request desktop site", {
                sessionManager.selectedSessionOrThrow.desktopMode
            }) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked)
            }.apply {
                visible = { sessionManager.selectedSession != null }
            },

            SimpleBrowserMenuItem("Add to homescreen") {
                MainScope().launch { webAppUseCases.addToHomescreen() }
            }.apply {
                visible = { webAppUseCases.isPinningSupported() }
            },

            SimpleBrowserMenuItem("Find in Page") {
                FindInPageIntegration.launch?.invoke()
            }.apply {
                visible = { sessionManager.selectedSession != null }
            },

            SimpleBrowserMenuItem("Add-ons") {
                val intent = Intent(context, AddonsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },

            SimpleBrowserMenuItem("Settings") {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
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

        sessionManager.register(sessionManagerObserver, view = this)

        tabButtonBox = qwantbar_button_tabs.findViewById(R.id.counter_box)
        tabButtonBar = qwantbar_button_tabs.findViewById(R.id.counter_bar)
        tabButtonText = qwantbar_button_tabs.findViewById(R.id.counter_text)

        reference = WeakReference(qwantbar_button_tabs)
        qwantbar_button_tabs.setCount(sessionManager.sessions.size)
        qwantbar_layout_tabs.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            this.emitOnTabsClicked()
        }
        qwantbar_button_tabs.contentDescription = context.getString(R.string.mozac_feature_tabs_toolbar_tabs_button)

        qwantbar_layout_bookmarks.setOnClickListener { this.emitOnBookmarksClicked() }
        qwantbar_layout_home.setOnClickListener { this.emitOnHomeClicked() }
        qwantbar_layout_back.setOnClickListener { this.emitOnBackClicked() }

        qwantbar_button_menu.menuBuilder = menuBuilder

        val session = sessionManager.selectedSession
        if (session != null && session.url == context.getString(R.string.homepage)) {
            this.setHighlight(QwantBarSelection.SEARCH)
        }
    }

    fun onTabsClicked(callback: () -> Unit) {
        tabCallbacks.add(callback)
    }

    fun onBookmarksClicked(callback: () -> Unit) {
        bookmarksCallbacks.add(callback)
    }

    fun onHomeClicked(callback: () -> Unit) {
        homeCallbacks.add(callback)
    }

    fun onBackClicked(callback: () -> Unit) {
        backCallbacks.add(callback)
    }

    fun setHighlight(selection: QwantBarSelection) {
        val colorDefault = ContextCompat.getColor(context, R.color.menu_items)
        val colorSelected = ContextCompat.getColor(context, R.color.menu_items_selected)

        qwantbar_button_home.setImageResource(if (selection == QwantBarSelection.SEARCH) R.drawable.ic_search_selected else R.drawable.ic_search)
        qwantbar_text_home.setTextColor(if (selection == QwantBarSelection.SEARCH) colorSelected else colorDefault)

        qwantbar_button_bookmarks.setImageResource(if (selection == QwantBarSelection.BOOKMARKS) R.drawable.ic_bookmark_selected else R.drawable.ic_bookmark)
        qwantbar_text_bookmarks.setTextColor(if (selection == QwantBarSelection.BOOKMARKS) colorSelected else colorDefault)

        val tabColor = if (selection == QwantBarSelection.TABS) colorSelected else colorDefault
        tabButtonBox?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_box, tabColor))
        tabButtonBar?.setImageDrawable(DrawableUtils.loadAndTintDrawable(context, R.drawable.mozac_ui_tabcounter_bar, tabColor))
        tabButtonText?.setTextColor(tabColor)
        qwantbar_text_tabs.setTextColor(if (selection == QwantBarSelection.TABS) colorSelected else colorDefault)
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

    private fun emitOnHomeClicked() {
        homeCallbacks.forEach {
            it.invoke()
        }
    }

    private fun emitOnBackClicked() {
        backCallbacks.forEach {
            it.invoke()
        }
    }

    private fun updateTabCount() {
        reference.get()?.setCountWithAnimation(sessionManager.sessions.size)
    }

    enum class LeftButtonType {
        HOME, BACK
    }

    fun setLeftButton(type: LeftButtonType) {
        qwantbar_layout_home.visibility = if (type == LeftButtonType.HOME) View.VISIBLE else View.GONE
        qwantbar_layout_back.visibility = if (type == LeftButtonType.BACK) View.VISIBLE else View.GONE
    }
}
