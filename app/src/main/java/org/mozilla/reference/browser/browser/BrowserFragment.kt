/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.search.SearchEngineParser
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents


/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<ThumbnailsFeature>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    private var toolbarSessionObserver: ToolbarSessionObserver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // engineView.setVerticalClipping(56.dpToPx(Resources.getSystem().displayMetrics)) // Qwant bar size
        toolbarSessionObserver = ToolbarSessionObserver(requireContext().components.core.sessionManager, toolbar)
        requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)

        val seInput = requireContext().assets.open("opensearch_qwant.xml")
        val seParser = SearchEngineParser()
        val searchEngine = seParser.load("qwant", seInput)

        if (requireContext().components.core.sessionManager.sessions.isEmpty()) {
            requireContext().components.useCases.tabsUseCases.addTab.invoke("https://www.qwant.com/", selectTab = true) // TODO move to variable
        }

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                searchEngine,
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        thumbnailsFeature.set(
                feature = ThumbnailsFeature(requireContext(),
                        engineView,
                        requireComponents.core.sessionManager),
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
    }

    companion object {
        fun create(sessionId: String? = null) = BrowserFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
