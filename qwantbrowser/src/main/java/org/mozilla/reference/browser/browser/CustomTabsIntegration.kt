/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.app.Activity
import android.content.Context
import android.content.Intent
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
// import mozilla.components.browser.session.SessionManager
// import mozilla.components.browser.toolbar.BrowserToolbar
import org.mozilla.reference.browser.compat.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import org.mozilla.reference.browser.compat.toolbar.CustomTabsToolbarFeature
// import org.mozilla.reference.browser.compat.toolbar.CustomTabsToolbarFeature
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.ext.components

class CustomTabsIntegration(
        context: Context,
        // sessionManager: SessionManager,
        toolbar: BrowserToolbar,
        engineView: EngineView,
        sessionUseCases: SessionUseCases,
        sessionId: String,
        activity: Activity?
) : LifecycleAwareFeature, UserInteractionHandler {

    // private val session = sessionManager.findSessionById(sessionId)
    private val logger = Logger("CustomTabsIntegration")

    init {
        /* if (session == null) {
            logger.warn("The session for this ID, no longer exists. Finishing activity.")
            activity?.finish()
        } */
        toolbar.display.setUrlBackground(null)
    }

    /* private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
            contentDescription = "Forward") {
            sessionUseCases.goForward.invoke(session)
        }

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
            contentDescription = "Refresh") {
            sessionUseCases.reload.invoke(session)
        }

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
            contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke(session)
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    } */

    private val menuItems by lazy {
        listOf(
            // menuToolbar,
            SimpleBrowserMenuItem("Share") {
                // session?.url?.let { context.share(it) }
            },

            /* BrowserMenuSwitch("Request desktop site", {
                session?.desktopMode ?: false
            }) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked, session)
            }, */

            SimpleBrowserMenuItem("Find in Page") {
                FindInPageIntegration.launch?.invoke()
            },

            SimpleBrowserMenuItem("Open in Browser") {
                // Release the session from this view so that it can immediately be rendered by a different view
                engineView.release()

                // Stip the CustomTabConfig to turn this Session into a regular tab and then select it
                /* sessionManager.findSessionById(sessionId)?.let { session ->
                    session.customTabConfig = null
                    sessionManager.select(session)
                } */

                // Close this activity since it is no longer displaying any session
                activity?.finish()

                // Now switch to the actual browser which should now display our new selected session
                val intent = Intent(context, BrowserActivity::class.java)
                context.startActivity(intent)
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    private val feature = CustomTabsToolbarFeature(
        store = context.components.core.store,
        toolbar = toolbar,
        sessionId = sessionId,
        useCases = context.components.useCases.customTabsUseCases,
        menuBuilder = menuBuilder,
        window = activity?.window,
        closeListener = { activity?.finish() }
    )

    override fun start() {
        feature.start()
    }

    override fun stop() {
        feature.stop()
    }

    override fun onBackPressed(): Boolean {
        return feature.onBackPressed()
    }
}
