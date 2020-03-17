/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.search.SearchEngineParser
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import java.lang.reflect.Method


/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<ThumbnailsFeature>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    private var toolbarSessionObserver: ToolbarSessionObserver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val engineSettings = requireContext().components.core.engine.settings
        if (engineSettings.userAgentString != null)
            if (!engineSettings.userAgentString!!.contains("QwantMobile")) {
                /* try {
                    val cl = context!!.classLoader
                    val systemProperties = cl.loadClass("android.os.SystemProperties")

                    //Parameters Types
                    val paramTypes: Array<Class<*>?> = arrayOfNulls(1)
                    paramTypes[0] = String::class.java
                    val get: Method = systemProperties.getMethod("get", *paramTypes)

                    //Parameters
                    val params = arrayOfNulls<Any>(1)
                    params[0] = "ro.HuaweiID.com.qwant.liberty"

                    val huaweiTrackingId = get.invoke(systemProperties, params) as String
                    engineSettings.userAgentString += " $huaweiTrackingId"
                } catch (iAE: IllegalArgumentException) {
                    Log.e("QWANT_BROWSER", "Illegal argument exception recovering huawei tracking id")
                } catch (e: Exception) {
                    Log.e("QWANT_BROWSER", "Exception recovering huawei tracking id")
                } */

                engineSettings.userAgentString += " h QwantMobile/4.0"
            }
        engineSettings.remoteDebuggingEnabled = false
        engineSettings.testingModeEnabled = false

        swipeRefresh.isEnabled = false

        toolbarSessionObserver = ToolbarSessionObserver(requireContext().components.core.sessionManager, toolbar, swipeRefresh)
        requireContext().components.core.sessionManager.register(this.toolbarSessionObserver!!)

        val searchEngine = SearchEngineParser().load("qwant", requireContext().assets.open("opensearch_qwant.xml"))

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                searchEngine,
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab
            )
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

        if (requireContext().components.core.sessionManager.sessions.none { !it.private }) {
            requireContext().components.useCases.tabsUseCases.addTab.invoke(QwantUtils.getHomepage(requireContext().applicationContext), selectTab = true)
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
