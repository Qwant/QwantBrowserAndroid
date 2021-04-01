package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.state.SessionState
import mozilla.components.lib.state.Observer
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.gecko.util.ThreadUtils.runOnUiThread
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.layout.QwantBar

/* class QwantBarSessionObserver(
        private val context: Context,
        private val sessionManager: SessionManager,
        private val qwantbar: QwantBar
) : Observer<SessionState> {
    private var textviewHome : TextView = qwantbar.findViewById(R.id.qwantbar_text_home)
    private var textviewBookmarks : View = qwantbar.findViewById(R.id.qwantbar_text_bookmarks)
    private var textviewTabs : View = qwantbar.findViewById(R.id.qwantbar_text_tabs)
    private var textviewMenu : View = qwantbar.findViewById(R.id.qwantbar_text_menu_qwant)
    private var imageviewHome : ImageView = qwantbar.findViewById(R.id.qwantbar_button_home)
    private var imageviewNavBack : ImageView = qwantbar.findViewById(R.id.qwantbar_button_nav_back)
    private var imageviewNavForward : ImageView = qwantbar.findViewById(R.id.qwantbar_button_nav_forward)

    private var layoutNavBack : View = qwantbar.findViewById(R.id.qwantbar_layout_nav_back)
    private var layoutNavForward : View = qwantbar.findViewById(R.id.qwantbar_layout_nav_forward)
    private var layoutBookmarks : View = qwantbar.findViewById(R.id.qwantbar_layout_bookmarks)
    private var layoutMenuQwant : View = qwantbar.findViewById(R.id.qwantbar_layout_menu_qwant)
    private var layoutMenuNav : View = qwantbar.findViewById(R.id.qwantbar_layout_menu_nav)



    init {
        checkSession()
    }



    /* override fun invoke(browserState: BrowserState) {
        checkSession()
    } */

    override fun invoke(session: SessionState) {
        if (sessionManager.selectedSession != null && session.id == sessionManager.selectedSession!!.id) {
            checkSession(sessionManager.selectedSession!!)
        }
    }
} */