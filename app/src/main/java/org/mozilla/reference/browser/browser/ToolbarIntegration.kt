/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
// import org.mozilla.reference.browser.compat.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
// import org.mozilla.reference.browser.compat.toolbar.DisplayToolbar
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.android.Padding
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.content.getColorFromAttr
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class ToolbarIntegration(
    context: Context,
    toolbar: BrowserToolbar,
    historyStorage: HistoryStorage,
    sessionManager: SessionManager,
    sessionUseCases: SessionUseCases,
    sessionId: String? = null
) : LifecycleAwareFeature, UserInteractionHandler {
    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    class PrivateBrowsingBrowserAction(context: Context, val sessionManager: SessionManager)
        : Toolbar.ActionImage(
            ContextCompat.getDrawable(context, R.drawable.icons_custom_privacy_fill)!!,
            padding = Padding(-5, 5, 0, 5)
    ) {
        override val visible: () -> Boolean
            get() = { if (sessionManager.selectedSession != null) sessionManager.selectedSession!!.private else false }
    }
    private val privateBrowsingBrowserAction = PrivateBrowsingBrowserAction(context, sessionManager)

    class ReloadPageAction(val sessionUseCases: SessionUseCases, drawable: Drawable)
        : Toolbar.ActionButton(
            drawable,
            "Refresh",
            padding = Padding(10, 10, 10, 10),
            visible = { true },
            listener = { sessionUseCases.reload() }
    )
    private val drawable = ContextCompat.getDrawable(context, R.drawable.icons_system_refresh_line)!!

    init {
        drawable.setTint(context.getColorFromAttr(R.attr.qwant_toolbar_IconsColor))
        toolbar.addPageAction(ReloadPageAction(sessionUseCases, drawable))
        toolbar.addBrowserAction(privateBrowsingBrowserAction)

        ToolbarAutocompleteFeature(toolbar).apply {
            addHistoryStorageProvider(historyStorage)
            addDomainProvider(shippedDomainsProvider)
        }

        toolbar.display.indicators = listOf(
            DisplayToolbar.Indicators.SECURITY,
            DisplayToolbar.Indicators.TRACKING_PROTECTION
        )
        toolbar.display.displayIndicatorSeparator = true

        toolbar.display.setOnSiteSecurityClickedListener { Log.d("QWANT_BROWSER", "click site security"); }
        toolbar.display.setOnTrackingProtectionClickedListener { Log.d("QWANT_BROWSER", "click tracking protection"); }

        toolbar.display.colors = toolbar.display.colors.copy(
            securityIconSecure = context.getColorFromAttr(R.attr.qwant_toolbar_SecureColor),
            securityIconInsecure = context.getColorFromAttr(R.attr.qwant_toolbar_InsecureColor),
            hint = context.getColorFromAttr(R.attr.qwant_toolbar_HintColor),
            text = context.getColorFromAttr(R.attr.qwant_toolbar_TextColor),
            trackingProtection = context.getColorFromAttr(R.attr.qwant_toolbar_IconsColor),
            separator = context.getColorFromAttr(R.attr.qwant_toolbar_TrackingProtectionAndSecurityIndicatorSeparatorColor)
        )
        toolbar.display.icons = toolbar.display.icons.copy(
            trackingProtectionException = ResourcesCompat.getDrawable(context.resources, R.drawable.icons_system_shield_warning_line, context.theme)!!,
            trackingProtectionNothingBlocked = ResourcesCompat.getDrawable(context.resources, R.drawable.icons_system_shield_off_line, context.theme)!!,
            trackingProtectionTrackersBlocked = ResourcesCompat.getDrawable(context.resources, R.drawable.icons_system_shield_line_2, context.theme)!!
        )
        toolbar.display.setUrlBackground(ResourcesCompat.getDrawable(context.resources, R.drawable.url_background, context.theme))

        toolbar.edit.hint = context.getString(R.string.toolbar_hint)
        /* toolbar.edit.colors = toolbar.edit.colors.copy( // TODO colors
            hint = context.getColorFromAttr(R.attr.qwant_color_light),
            text = context.getColorFromAttr(R.attr.qwant_toolbar_TextColor),
            suggestionBackground = context.getColorFromAttr(R.attr.qwant_color_selected),
            suggestionForeground = context.getColorFromAttr(R.attr.qwant_color_selected_text)
        ) */
    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        context.components.core.store,
        context.components.useCases.sessionUseCases.loadUrl,
        { searchTerms ->
            context.components.useCases.searchUseCases.defaultSearch.invoke(
                searchTerms = searchTerms,
                searchEngine = null,
                parentSessionId = null
        ) },
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
