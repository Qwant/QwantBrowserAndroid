/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.view.BrowserMenuSwitch

class ToolbarIntegration(
    context: Context,
    toolbar: BrowserToolbar,
    historyStorage: HistoryStorage,
    sessionManager: SessionManager,
    sessionUseCases: SessionUseCases,
    webAppUseCases: WebAppUseCases,
    sessionId: String? = null
) : LifecycleAwareFeature, UserInteractionHandler {
    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = R.color.qwant_main,
            contentDescription = "Forward",
            isEnabled = { sessionManager.selectedSession?.canGoForward == true }) {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = R.color.qwant_main,
            contentDescription = "Refresh") {
            sessionUseCases.reload.invoke()
        }

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = R.color.qwant_main,
            contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke()
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    private val menuItems: List<BrowserMenuItem> by lazy {
        listOf(
            menuToolbar,
                SimpleBrowserMenuItem(context.getString(R.string.context_menu_share), textColorResource = R.color.qwant_main) {
                    val url = sessionManager.selectedSession?.url ?: ""
                    context.share(url)
                }.apply {
                    visible = { sessionManager.selectedSession != null }
                },

                BrowserMenuSwitch(context.getString(R.string.context_menu_request_desktop), {
                    sessionManager.selectedSessionOrThrow.desktopMode
                }) { checked ->
                    sessionUseCases.requestDesktopSite.invoke(checked)
                }.apply {
                    visible = { sessionManager.selectedSession != null }
                },

                SimpleBrowserMenuItem(context.getString(R.string.context_menu_add_homescreen), textColorResource = R.color.qwant_main) {
                    MainScope().launch { webAppUseCases.addToHomescreen() }
                }.apply {
                    visible = { webAppUseCases.isPinningSupported() }
                },

                SimpleBrowserMenuItem(context.getString(R.string.context_menu_find), textColorResource = R.color.qwant_main) {
                    FindInPageIntegration.launch?.invoke()
                }.apply {
                    visible = { sessionManager.selectedSession != null }
                },

                SimpleBrowserMenuItem(context.getString(R.string.context_menu_addons), textColorResource = R.color.qwant_main) {
                    val intent = Intent(context, AddonsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                },

                SimpleBrowserMenuItem(context.getString(R.string.settings), textColorResource = R.color.qwant_main) {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    init {
        toolbar.display.indicators = listOf(
            DisplayToolbar.Indicators.SECURITY,
            DisplayToolbar.Indicators.TRACKING_PROTECTION
        )
        toolbar.display.displayIndicatorSeparator = true

        toolbar.display.menuBuilder = menuBuilder // TODO remove this to remove top menu

        toolbar.display.hint = context.getString(R.string.toolbar_hint)
        toolbar.edit.hint = context.getString(R.string.toolbar_hint)

        ToolbarAutocompleteFeature(toolbar).apply {
            addHistoryStorageProvider(historyStorage)
            addDomainProvider(shippedDomainsProvider)
        }

        toolbar.edit.colors = toolbar.edit.colors.copy(
            clear = ContextCompat.getColor(context, R.color.qwant_main),
            hint = ContextCompat.getColor(context, R.color.qwant_light),
            text = ContextCompat.getColor(context, R.color.qwant_main),
            suggestionBackground = ContextCompat.getColor(context, R.color.qwant_selected),
            suggestionForeground = ContextCompat.getColor(context, R.color.photonWhite)
        )
        toolbar.display.setUrlBackground(context.resources.getDrawable(R.drawable.url_background, context.theme))

        toolbar.display.colors = toolbar.display.colors.copy(
            securityIconSecure = ContextCompat.getColor(context, R.color.photonGreen70),
            securityIconInsecure = ContextCompat.getColor(context, R.color.photonRed70),
            emptyIcon = ContextCompat.getColor(context, R.color.qwant_main),
            menu = ContextCompat.getColor(context, R.color.qwant_main),
            hint = ContextCompat.getColor(context, R.color.qwant_selected),
            title = ContextCompat.getColor(context, R.color.qwant_main),
            text = ContextCompat.getColor(context, R.color.qwant_main),
            trackingProtection = ContextCompat.getColor(context, R.color.qwant_main),
            separator = ContextCompat.getColor(context, R.color.qwant_main)
        )
        toolbar.display.setUrlBackground(context.resources.getDrawable(R.drawable.url_background, context.theme))
1    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        context.components.core.store,
        context.components.useCases.sessionUseCases.loadUrl,
        { searchTerms -> context.components.useCases.searchUseCases.defaultSearch.invoke(searchTerms) },
        sessionId
    )

    override fun start() {
        toolbarFeature.start()
    }

    override fun stop() {
        toolbarFeature.stop()
    }

    override fun onBackPressed(): Boolean {
        return toolbarFeature.onBackPressed()
    }
}
