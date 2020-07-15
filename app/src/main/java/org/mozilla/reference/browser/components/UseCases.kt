/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.usecases.EngineSessionUseCases
import mozilla.components.browser.state.store.BrowserStore
// import mozilla.components.browser.thumbnails.ThumbnailsUseCases
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.Settings
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.contextmenu.ContextMenuUseCases
/* import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.WebAppShortcutManager
import mozilla.components.feature.pwa.WebAppUseCases */
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SettingsUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * Component group for all use cases. Use cases are provided by feature
 * modules and can be triggered by UI interactions.
 */
class UseCases(
        private val context: Context,
        private val sessionManager: SessionManager,
        private val store: BrowserStore,
        private val engine: Engine,
        private val searchEngineManager: SearchEngineManager,
        private val client: Client,
        private val thumbnailStorage: ThumbnailStorage
) {
    /**
     * Use cases that provide engine interactions for a given browser session.
     */
    val sessionUseCases by lazy { SessionUseCases(sessionManager) }

    val engineSessionUseCases by lazy { EngineSessionUseCases(sessionManager) }

    /**
     * Use cases that provide tab management.
     */
    val tabsUseCases: TabsUseCases by lazy { TabsUseCases(store, sessionManager) }

    /**
     * Use cases that provide search engine integration.
     */
    val searchUseCases by lazy { SearchUseCases(context, searchEngineManager, sessionManager) }

    /**
     * Use cases that provide settings management.
     */
    val settingsUseCases by lazy { SettingsUseCases(engine, store) }

    /**
     * Use cases that provide shortcut and progressive web app management.
     */
    /* val webAppUseCases by lazy {
        WebAppUseCases(context, sessionManager, client)
        // TODO use directly this to fix deprecated: WebAppShortcutManager(context, client, ManifestStorage(context), true)
    } */

    /**
     * Uses cases that provides context menu
     */
    val contextMenuUseCases: ContextMenuUseCases by lazy { ContextMenuUseCases(sessionManager, store) }

    /* val thumbnailUseCases: ThumbnailsUseCases by lazy {
        ThumbnailsUseCases(
                store,
                thumbnailStorage
        )
    } */
}
