/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.compat

import android.content.Context
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.QwantUtils

/**
 * Contains use cases related to the search feature.
 */
class QwantSearchUseCases(
        context: Context,
        store: BrowserStore,
        tabsUseCases: TabsUseCases
) {
    class QwantDefaultSearchUseCase(
            private val context: Context,
            private val store: BrowserStore,
            private val tabsUseCases: TabsUseCases
    ) : SearchUseCases.SearchUseCase {
        /**
         * Triggers a search in the currently selected session.
         */
        override fun invoke(
                searchTerms: String,
                searchEngine: SearchEngine?,
                parentSessionId: String?
        ) {
            invoke(searchTerms, store.state.selectedTabId, searchEngine)
        }

        /**
         * Triggers a search using the default search engine for the provided search terms.
         *
         * @param searchTerms the search terms.
         * @param sessionId the ID of the session/tab to use, or null if the currently selected tab
         * should be used.
         * @param searchEngine Search Engine to use, or the default search engine if none is provided
         */
        operator fun invoke(
                searchTerms: String,
                sessionId: String? = store.state.selectedTabId,
                searchEngine: SearchEngine? = null
        ) {
            val searchUrl = QwantUtils.getHomepage(context = context, query = searchTerms)

            val id = if (sessionId == null) {
                // If no `sessionId` was passed in then create a new tab
                tabsUseCases.addTab(searchUrl)
            } else {
                // If we got a `sessionId` then try to find the tab and load the search URL in it
                val existingTab = store.state.findTabOrCustomTab(sessionId)
                if (existingTab != null) {
                    store.dispatch(
                            EngineAction.LoadUrlAction(
                                    existingTab.id,
                                    searchUrl
                            )
                    )
                    existingTab.id
                } else {
                    // If the tab with the provided id was not found then create a new tab
                    tabsUseCases.addTab(searchUrl)
                }
            }

            store.dispatch(ContentAction.UpdateSearchTermsAction(id, searchTerms))
        }
    }

    class NewTabSearchUseCase(
            private val context: Context,
            private val store: BrowserStore,
            private val tabsUseCases: TabsUseCases,
            private val isPrivate: Boolean
    ) : SearchUseCases.SearchUseCase {
        override fun invoke(
                searchTerms: String,
                searchEngine: SearchEngine?,
                parentSessionId: String?
        ) {
            invoke(
                    searchTerms,
                    source = SessionState.Source.NONE,
                    selected = true,
                    private = isPrivate,
                    searchEngine = searchEngine,
                    parentSessionId = parentSessionId
            )
        }

        /**
         * Triggers a search on a new session, using the default search engine for the provided search terms.
         *
         * @param searchTerms the search terms.
         * @param selected whether or not the new session should be selected, defaults to true.
         * @param private whether or not the new session should be private, defaults to false.
         * @param source the source of the new session.
         * @param searchEngine Search Engine to use, or the default search engine if none is provided
         * @param parentSessionId optional parent session to attach this new search session to
         */
        @Suppress("LongParameterList")
        operator fun invoke(
                searchTerms: String,
                source: SessionState.Source,
                selected: Boolean = true,
                private: Boolean = false,
                searchEngine: SearchEngine? = null,
                parentSessionId: String? = null
        ) {
            val searchUrl = QwantUtils.getHomepage(context = context, query = searchTerms)

            var id = ""
            if (private) {
                id = tabsUseCases.addTab(
                        url = searchUrl,
                        startLoading = true,
                        parentId = parentSessionId,
                        source = source,
                        selectTab = selected
                )
            } else {
                id = tabsUseCases.addPrivateTab(
                        url = searchUrl,
                        startLoading = true,
                        parentId = parentSessionId,
                        source = source,
                        selectTab = selected
                )
            }

            store.dispatch(ContentAction.UpdateSearchTermsAction(id, searchTerms))
        }
    }

    val defaultSearch: QwantDefaultSearchUseCase by lazy {
        QwantDefaultSearchUseCase(context, store, tabsUseCases)
    }

    val newTabSearch: NewTabSearchUseCase by lazy {
        NewTabSearchUseCase(context, store, tabsUseCases, isPrivate = true)
    }
}
