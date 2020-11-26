/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveModeIfNeeded
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.qwant.ToolbarControlFeature


/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    // private var toolbarControlFeature = ViewBoundFeatureWrapper<ToolbarControlFeature>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh.isEnabled = false

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                    requireContext(),
                    requireComponents.core.store,
                    requireComponents.useCases.searchUseCases.defaultSearch,
                    fetchClient = requireComponents.core.client,
                    mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                    engine = requireComponents.core.engine,
                    filterExactMatch = true
            )
            /* .addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl) */
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        thumbnailsFeature.set(
                feature = BrowserThumbnails(requireContext(),
                    engineView,
                    requireComponents.core.store),
                owner = this,
                view = view
        )

        webExtToolbarFeature.set(
            feature = WebExtensionToolbarFeature(
                view.toolbar,
                requireContext().components.core.store
            ),
            owner = this,
            view = view
        )

        /* toolbarControlFeature.set(
            feature = ToolbarControlFeature(
                requireContext(),
                requireComponents.core.store,
                toolbar
            ),
            owner = this,
            view = view
        ) */
    }

    override fun fullScreenChanged(enabled: Boolean) {
        (activity as BrowserActivity).fullScreenChanged(enabled)
        if (enabled) {
            toolbar.visibility = View.GONE
            swipeRefresh.setPadding(0, 0, 0, 0)
            activity?.enterToImmersiveMode()
        } else {
            toolbar.visibility = View.VISIBLE
            swipeRefresh.setPadding(0, 56.dpToPx(Resources.getSystem().displayMetrics), 0, 0)
            activity?.exitImmersiveModeIfNeeded()

            // Restore system UI colors
            activity?.window?.statusBarColor = requireContext().getColorFromAttr(R.attr.qwant_color_background_dark)
            activity?.window?.navigationBarColor = requireContext().getColorFromAttr(R.attr.qwant_color_background_dark)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                activity?.window?.decorView?.systemUiVisibility = flags
            }
        }
    }

    override fun onPictureInPictureModeChanged(enabled: Boolean) {
        val session = requireComponents.core.store.state.selectedTab
        val fullScreenMode = session?.content?.fullScreen ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    fun closeAwesomeBarIfOpen() {
        if (awesomeBar != null && awesomeBar.isShown) {
            this.onBackPressed()
        }
    }

    companion object {
        fun create(sessionId: String? = null) = BrowserFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
