package org.mozilla.reference.browser.layout

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.component_qwantbar.view.*
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.application
import java.lang.ref.WeakReference

class QwantBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var reference: WeakReference<TabCounter> = WeakReference<TabCounter>(null)
    private val sessionManager: SessionManager = context.applicationContext.application.components.core.sessionManager
    private val tabCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val bookmarksCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val homeCallbacks: MutableList<() -> Unit> = mutableListOf()

    private val sessionManagerObserver = object : SessionManager.Observer {
        override fun onSessionAdded(session: Session) { updateTabCount() }
        override fun onSessionRemoved(session: Session) { updateTabCount() }
        override fun onSessionsRestored() { updateTabCount() }
        override fun onAllSessionsRemoved() { updateTabCount() }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_qwantbar, this, true)

        sessionManager.register(sessionManagerObserver, view = this)

        reference = WeakReference(qwantbar_button_tabs)
        qwantbar_button_tabs.setCount(sessionManager.sessions.size)
        qwantbar_button_tabs.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            this.emitOnTabsClicked()
        }
        qwantbar_button_tabs.contentDescription = context.getString(R.string.mozac_feature_tabs_toolbar_tabs_button)

        qwantbar_button_bookmarks.setOnClickListener { this.emitOnBookmarksClicked() }
        qwantbar_button_home.setOnClickListener { this.emitOnHomeClicked() }
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

    private fun updateTabCount() {
        reference.get()?.setCountWithAnimation(sessionManager.sessions.size)
    }

}
