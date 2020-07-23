/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.search.SearchEngine
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents


/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    // private var toolbarSessionObserver: ToolbarSessionObserver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh.isEnabled = false

        // toolbarSessionObserver = ToolbarSessionObserver(requireContext().components.core.sessionManager, toolbar, swipeRefresh)
        // requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireComponents.search.searchEngineManager.defaultSearchEngine!!,
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
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

        if (requireContext().components.core.sessionManager.sessions.none { !it.private }) {
            requireContext().components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(requireContext().applicationContext))
        }
    }

    /* override fun onPause() {
        super.onPause()
        requireContext().components.core.sessionManager.unregister(this.toolbarSessionObserver!!)
    }

    override fun onResume() {
        super.onResume()
        requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)
    } */

    override fun fullScreenChanged(enabled: Boolean) {
        (activity as BrowserActivity).fullScreenChanged(enabled)
        if (enabled) {
            toolbar.visibility = View.GONE
            swipeRefresh.setPadding(0, 0, 0, 0)
        } else {
            toolbar.visibility = View.VISIBLE
            swipeRefresh.setPadding(0, 56.dpToPx(Resources.getSystem().displayMetrics), 0, 0)
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

    companion object {
        fun create(sessionId: String? = null) = BrowserFragment().apply {
            Log.d("QWANT_BROWSER", "browser fragment - create")
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
