/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
// import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
// import mozilla.components.feature.toolbar.ToolbarFeature
import org.mozilla.reference.browser.compat.ToolbarFeature
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.android.Padding
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import java.net.URLDecoder

class ToolbarIntegration(
    val context: Context,
    val toolbar: BrowserToolbar,
    historyStorage: HistoryStorage,
    sessionUseCases: SessionUseCases,
    sessionId: String? = null
) : LifecycleAwareFeature, UserInteractionHandler {
    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    class PrivateBrowsingBrowserAction(context: Context, val store: BrowserStore)
        : Toolbar.ActionImage(
            ContextCompat.getDrawable(context, R.drawable.icons_custom_privacy_fill)!!,
            padding = Padding(-5, 5, 0, 5)
    ) {
        override val visible: () -> Boolean
            get() = { store.state.selectedTab?.content?.private ?: false }
    }
    private val privateBrowsingBrowserAction = PrivateBrowsingBrowserAction(context, context.components.core.store)

    class ReloadPageAction(val sessionUseCases: SessionUseCases, drawable: Drawable)
        : Toolbar.ActionButton(
            drawable,
            "Refresh",
            padding = Padding(10, 10, 10, 10),
            listener = { sessionUseCases.reload() }
    )
    class CancelLoadPageAction(val sessionUseCases: SessionUseCases, drawable: Drawable)
        : Toolbar.ActionButton(
            drawable,
            "Cancel",
            padding = Padding(10, 10, 10, 10),
            listener = { sessionUseCases.stopLoading() }
    )

    private val drawableReload = ContextCompat.getDrawable(context, R.drawable.icons_system_refresh_line)!!
    private val reloadAction = ReloadPageAction(sessionUseCases, drawableReload)
    private val drawableCancelLoad = ContextCompat.getDrawable(context, R.drawable.close_cross)!!
    private val cancelLoadAction = CancelLoadPageAction(sessionUseCases, drawableCancelLoad)
    private var loadingStateChangedScope: CoroutineScope? = null

    init {
        drawableReload.setTint(context.getColorFromAttr(R.attr.qwant_toolbar_IconsColor))
        drawableCancelLoad.setTint(context.getColorFromAttr(R.attr.qwant_toolbar_IconsColor))

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


        toolbar.edit.setOnEditFocusChangeListener { hasFocus ->
            val url = context.components.core.store.state.selectedTab?.content?.url
            if (hasFocus && url?.startsWith(context.getString(R.string.homepage_startwith_filter)) == true) {
                if (url.contains("?q=") || url.contains("&q=")) {
                    val query = url.split("?q=", "&q=")[1].split("&")[0]
                    toolbar.edit.updateUrl(URLDecoder.decode(query, "UTF-8"))
                } else {
                    toolbar.edit.updateUrl("")
                }
            }
        }

        toolbar.edit.hint = context.getString(R.string.toolbar_hint)
        toolbar.edit.colors = toolbar.edit.colors.copy(
            text = context.getColorFromAttr(R.attr.qwant_toolbar_EditTextColor),
            hint = context.getColorFromAttr(R.attr.qwant_toolbar_HintColor),
            suggestionBackground = context.getColorFromAttr(R.attr.qwant_toolbar_SuggestionBackgroundColor),
            suggestionForeground = context.getColorFromAttr(R.attr.qwant_toolbar_SuggestionForegroundColor)
        )
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

    private var pageActionReloadState = false
    private var pageActionCancelState = false
    override fun start() {
        toolbarFeature.start()
        loadingStateChangedScope = context.components.core.store.flowScoped { flow -> flow
            .mapNotNull { state -> state.selectedTab?.content?.loading }
            .ifChanged()
            .collect {
                if (it) {
                    toolbar.display.indicators = listOf(
                            DisplayToolbar.Indicators.TRACKING_PROTECTION
                    )
                    toolbar.display.displayIndicatorSeparator = false
                    if (pageActionReloadState) {
                        toolbar.removePageAction(reloadAction)
                        pageActionReloadState = false
                    }
                    if (!pageActionCancelState) {
                        toolbar.addPageAction(cancelLoadAction)
                        pageActionCancelState = true
                    }
                } else {
                    toolbar.display.indicators = listOf(
                            DisplayToolbar.Indicators.SECURITY,
                            DisplayToolbar.Indicators.TRACKING_PROTECTION
                    )
                    toolbar.display.displayIndicatorSeparator = true
                    if (!pageActionReloadState) {
                        toolbar.addPageAction(reloadAction)
                        pageActionReloadState = true
                    }
                    if (pageActionCancelState) {
                        toolbar.removePageAction(cancelLoadAction)
                        pageActionCancelState = false
                    }
                }
            }
        }
    }

    override fun stop() {
        toolbarFeature.stop()
        loadingStateChangedScope?.cancel()
    }

    override fun onBackPressed(): Boolean {
        return toolbarFeature.onBackPressed()
    }
}
