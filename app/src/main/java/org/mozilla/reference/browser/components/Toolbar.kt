/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.content.Intent
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuCheckbox
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity

/**
 * Component group for all functionality related to the browser toolbar.
 */
class Toolbar(
    private val context: Context,
    private val sessionUseCases: SessionUseCases,
    private val sessionManager: SessionManager
) {

    /**
     * Helper class for building browser menus.
     */
    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    /**
     * Provides autocomplete functionality for shipped / provided domain lists.
     */
    val shippedDomainsProvider by lazy {
        ShippedDomainsProvider().also { it.initialize(context) }
    }

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
                iconTintColorResource = R.color.icons,
                contentDescription = "Forward") {
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

    private val menuItems by lazy {
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem("Share") {
                val url = sessionManager.selectedSession?.url ?: ""
                context.share(url)
            },
            SimpleBrowserMenuItem("Settings") {
                openSettingsActivity()
            },
            SimpleBrowserMenuItem("Clear Data") {
                sessionUseCases.clearData.invoke()
            },
            SimpleBrowserMenuCheckbox("Request desktop site") { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked)
            }
        )
    }

    private fun openSettingsActivity() {
        val intent = Intent(context, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
