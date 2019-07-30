/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.findinpage.view.FindInPageView
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.session.WindowFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.support.base.feature.BackHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveModeIfNeeded
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_APP_PERMISSIONS
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_PROMPT_PERMISSIONS
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.UserInteractionHandler
import org.mozilla.reference.browser.downloads.DownloadService
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.pip.PictureInPictureIntegration
import org.mozilla.reference.browser.tabs.TabsTrayFragment

@Suppress("TooManyFunctions")
class BrowserFragment : Fragment(), BackHandler, UserInteractionHandler {
    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val contextMenuIntegration = ViewBoundFeatureWrapper<ContextMenuIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val promptsFeature = ViewBoundFeatureWrapper<PromptFeature>()
    private val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    private val customTabsIntegration = ViewBoundFeatureWrapper<CustomTabsIntegration>()
    private val findInPageIntegration = ViewBoundFeatureWrapper<FindInPageIntegration>()
    private val sitePermissionFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    private val pictureInPictureIntegration = ViewBoundFeatureWrapper<PictureInPictureIntegration>()
    private val thumbnailsFeature = ViewBoundFeatureWrapper<ThumbnailsFeature>()
    private val readerViewFeature = ViewBoundFeatureWrapper<ReaderViewIntegration>()
    private val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    private val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()

    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        readerViewFeature,
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature,
        customTabsIntegration
    )

    private val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                engineView,
                sessionId),
            owner = this,
            view = view)

        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                toolbar,
                requireComponents.core.historyStorage,
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.webAppUseCases,
                sessionId),
            owner = this,
            view = view)

        contextMenuIntegration.set(
            feature = ContextMenuIntegration(
                requireContext(),
                requireFragmentManager(),
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases,
                view,
                sessionId),
            owner = this,
            view = view)

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireComponents.search.searchEngineManager.getDefaultSearchEngine(requireContext()),
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            sessionManager = requireComponents.core.sessionManager,
            showTabs = ::showTabs)

        downloadsFeature.set(
            feature = DownloadsFeature(
                requireContext(),
                sessionManager = requireComponents.core.sessionManager,
                sessionId = sessionId,
                fragmentManager = childFragmentManager,
                downloadManager = FetchDownloadManager(
                    requireContext().applicationContext,
                    DownloadService::class
                ),
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                }),
            owner = this,
            view = view)

        promptsFeature.set(
            feature = PromptFeature(
                fragment = this,
                sessionManager = requireComponents.core.sessionManager,
                sessionId = sessionId,
                fragmentManager = requireFragmentManager(),
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                }),
            owner = this,
            view = view)

        windowFeature.set(
            feature = WindowFeature(requireComponents.core.sessionManager),
            owner = this,
            view = view
        )

        fullScreenFeature.set(
            feature = FullScreenFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                sessionId, ::fullScreenChanged),
            owner = this,
            view = view)

        sessionId?.let { id ->
            customTabsIntegration.set(
                feature = CustomTabsIntegration(
                    requireContext(),
                    requireComponents.core.sessionManager,
                    toolbar,
                    engineView,
                    requireComponents.useCases.sessionUseCases,
                    id,
                    activity
                ),
                owner = this,
                view = view
            )
        }

        findInPageIntegration.set(
            feature = FindInPageIntegration(
                requireComponents.core.sessionManager,
                sessionId,
                findInPageBar as FindInPageView,
                engineView),
            owner = this,
            view = view)

        sitePermissionFeature.set(
            feature = SitePermissionsFeature(
                context = requireContext(),
                fragmentManager = requireFragmentManager(),
                sessionManager = requireComponents.core.sessionManager,
                sessionId = sessionId
            ) { permissions ->
                requestPermissions(permissions, REQUEST_CODE_APP_PERMISSIONS)
            },
            owner = this,
            view = view
        )

        pictureInPictureIntegration.set(
            feature = PictureInPictureIntegration(
                requireComponents.core.sessionManager,
                requireActivity()
            ),
            owner = this,
            view = view
        )

        thumbnailsFeature.set(
                feature = ThumbnailsFeature(requireContext(),
                        engineView,
                        requireComponents.core.sessionManager),
                owner = this,
                view = view
        )

        readerViewFeature.set(
            feature = ReaderViewIntegration(
                requireContext(),
                requireComponents.core.engine,
                requireComponents.core.sessionManager,
                view.toolbar,
                view.readerViewBar,
                view.readerViewAppearanceButton
            ),
            owner = this,
            view = view
        )

        swipeRefreshFeature.set(
            feature = SwipeRefreshFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases.reload,
                view.swipeRefresh
            ),
            owner = this,
            view = view
        )
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled) {
            activity?.enterToImmersiveMode()
            toolbar.visibility = View.GONE
        } else {
            activity?.exitImmersiveModeIfNeeded()
            toolbar.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed(): Boolean {
        return backButtonHandler.any { it.onBackPressed() }
    }

    override fun onHomePressed(): Boolean {
        return pictureInPictureIntegration.get()?.onHomePressed() ?: false
    }

    override fun onPictureInPictureModeChanged(enabled: Boolean) {
        val fullScreenMode = requireComponents.core.sessionManager.selectedSession?.fullScreenMode ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
            REQUEST_CODE_APP_PERMISSIONS -> sitePermissionFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
        }
    }

    companion object {
        private const val SESSION_ID = "session_id"

        fun create(sessionId: String? = null): BrowserFragment = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(SESSION_ID, sessionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        promptsFeature.withFeature { it.onActivityResult(requestCode, resultCode, data) }
    }
}
