package org.mozilla.reference.browser.compat
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.concept.toolbar.Toolbar.Highlight
import mozilla.components.concept.toolbar.Toolbar.SiteTrackingProtection
// import mozilla.components.feature.toolbar.internal.URLRenderer
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged

/**
 * Presenter implementation for a toolbar implementation in order to update the toolbar whenever
 * the state of the selected session.
 */
class ToolbarPresenter(
        private val toolbar: Toolbar,
        private val store: BrowserStore,
        private val customTabId: String? = null,
        urlRenderConfiguration: ToolbarFeature.UrlRenderConfiguration? = null
) {
    @VisibleForTesting
    internal var renderer = URLRenderer(toolbar, urlRenderConfiguration)

    private var scope: CoroutineScope? = null

    /**
     * Start presenter: Display data in toolbar.
     */
    fun start() {
        renderer.start()

        scope = store.flowScoped { flow ->
            flow.ifChanged { it.findCustomTabOrSelectedTab(customTabId) }
                    .collect { state ->
                        render(state)
                    }
        }
    }

    fun stop() {
        scope?.cancel()
        renderer.stop()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun render(state: BrowserState) {
        val tab = state.findCustomTabOrSelectedTab(customTabId)

        if (tab != null) {
            renderer.post(tab.content.url)

            toolbar.setSearchTerms(tab.content.searchTerms)
            toolbar.displayProgress(tab.content.progress)

            toolbar.siteSecure = if (!tab.content.securityInfo.secure && !tab.content.loading) {
                Log.d("QWANT_BROWSER", "site insecure (${tab.content.loading})")
                Toolbar.SiteSecurity.INSECURE
            } else {
                Log.d("QWANT_BROWSER", "site secure (${tab.content.loading})")
                Toolbar.SiteSecurity.SECURE
            }

            toolbar.siteTrackingProtection = when {
                tab.trackingProtection.ignoredOnTrackingProtection -> SiteTrackingProtection.OFF_FOR_A_SITE
                tab.trackingProtection.enabled && tab.trackingProtection.blockedTrackers.isNotEmpty() ->
                    SiteTrackingProtection.ON_TRACKERS_BLOCKED

                tab.trackingProtection.enabled -> SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED

                else -> SiteTrackingProtection.OFF_GLOBALLY
            }

            updateHighlight(tab)
        } else {
            clear()
        }
    }

    private fun updateHighlight(tab: SessionState) {
        toolbar.highlight = Highlight.NONE /* when {
            tab.content.permissionHighlights.permissionsChanged ||
                    tab.trackingProtection.ignoredOnTrackingProtection
            -> Highlight.PERMISSIONS_CHANGED
            else -> Highlight.NONE
        } */
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun clear() {
        Log.d("QWANT_BROWSER", "clear toolbar")
        renderer.post("")

        toolbar.setSearchTerms("")
        toolbar.displayProgress(0)

        toolbar.siteSecure = Toolbar.SiteSecurity.SECURE

        toolbar.siteTrackingProtection = SiteTrackingProtection.OFF_GLOBALLY
        toolbar.highlight = Highlight.NONE
    }
}