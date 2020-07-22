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
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.search.SearchEngineParser
import mozilla.components.browser.session.Session
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.feature.awesomebar.AwesomeBarFeature
// import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.concept.engine.EngineSession
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents


/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    private var toolbarSessionObserver: ToolbarSessionObserver? = null

    private var searchEngine: SearchEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO move this to core initialisation
        /*val engineSettings = requireContext().components.core.engine.settings
        if (engineSettings.userAgentString != null)
            if (!engineSettings.userAgentString!!.contains("QwantMobile")) {
                engineSettings.userAgentString += " h QwantMobile/4.0"
            }
        engineSettings.remoteDebuggingEnabled = false
        engineSettings.testingModeEnabled = false */

        // searchEngine = SearchEngineParser().load("qwant", requireContext().assets.open("opensearch_qwant.xml"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Log.d("QWANT_BROWSER", "UA - view created")
        val engineSettings = requireContext().components.core.engine.settings
        if (engineSettings.userAgentString != null) {
            Log.d("QWANT_BROWSER", "UA - 1")
            if (!engineSettings.userAgentString!!.contains("QwantMobile")) {
                Log.d("QWANT_BROWSER", "UA - 2")
                engineSettings.userAgentString += " h QwantMobile/4.0"
            } else {
                Log.d("QWANT_BROWSER", "UA - 3")
            }
        } else {
            Log.d("QWANT_BROWSER", "UA - 4")
        } */
        // engineSettings.remoteDebuggingEnabled = false
        // engineSettings.testingModeEnabled = false
        // requireContext().components.core.sessionManager.getEngineSession(requireContext().components.core.sessionManager.selectedSession!!)!!.settings.userAgentString

        swipeRefresh.isEnabled = false

        toolbarSessionObserver = ToolbarSessionObserver(requireContext().components.core.sessionManager, toolbar, swipeRefresh)
        requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)

        // val searchEngine = SearchEngineParser().load("qwant", requireContext().assets.open("opensearch_qwant.xml"))

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

            /* requireContext().components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(requireContext().applicationContext), selectTab = true)
            val engineSession: EngineSession = requireContext().components.core.sessionManager.getOrCreateEngineSession()
            Log.d("QWANT_BROWSER", "First tab engine ua: ${engineSession.settings.userAgentString}")
            requireContext().components.useCases.sessionUseCases.reload()
            Log.d("QWANT_BROWSER", "First tab reloaded") */

            /* val url = QwantUtils.getHomepage(requireContext().applicationContext)
            val session = Session(url, false)

            requireContext().components.core.sessionManager.add(session, selected = true)
            val engineSession = requireContext().components.core.sessionManager.getOrCreateEngineSession(session)
            Log.d("QWANT_BROWSER", "First tab engine ua: ${engineSession.settings.userAgentString}")
            engineSession.loadUrl(url) */

            // engineSession.reload()
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().components.core.sessionManager.unregister(this.toolbarSessionObserver!!)
    }

    override fun onResume() {
        super.onResume()
        requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)
    }

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
