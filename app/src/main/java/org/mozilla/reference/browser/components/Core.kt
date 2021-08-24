/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import mozilla.components.browser.session.Session
import org.mozilla.reference.browser.browser.icons.BrowserIcons
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.engine.EngineMiddleware
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.ThumbnailsMiddleware
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.update.AddonUpdater
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.feature.customtabs.store.CustomTabsServiceStore
import mozilla.components.feature.downloads.DownloadMiddleware
import mozilla.components.feature.media.MediaSessionFeature
import org.mozilla.reference.browser.downloads.DownloadService
import org.mozilla.reference.browser.media.MediaSessionService
import mozilla.components.feature.readerview.ReaderViewMiddleware
import mozilla.components.feature.media.middleware.RecordingDevicesMiddleware
import mozilla.components.feature.session.HistoryDelegate
import org.mozilla.reference.browser.browser.WebNotificationFeature
import org.mozilla.reference.browser.AppRequestInterceptor
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.EngineProvider
import org.mozilla.reference.browser.storage.history.History
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_normal
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_private
import java.util.concurrent.TimeUnit
import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.WebAppShortcutManager
// import mozilla.components.feature.search.middleware.SearchMiddleware
import mozilla.components.feature.search.region.RegionMiddleware
import mozilla.components.feature.sitepermissions.SitePermissionsStorage
import mozilla.components.service.location.LocationService
import org.mozilla.reference.browser.addons.QwantAddonCollectionProvider


/**
 * Component group for all core browser functionality.
 */
class Core(private val context: Context) {
    /**
     * The browser engine component initialized based on the build
     * configuration (see build variants).
     */
    val engine: Engine by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(context),
            remoteDebuggingEnabled = false, // prefs.getBoolean(context.getPreferenceKey(pref_key_remote_debugging), false),
            trackingProtectionPolicy = createTrackingProtectionPolicy(prefs),
            historyTrackingDelegate = HistoryDelegate(lazy { historyStorage }),
            testingModeEnabled = false,
            userAgentString = "Mozilla/5.0 (Android 10; Mobile; rv:77.0) Gecko/77.0 Firefox/77.0 QwantMobile/4.0"
        )
        EngineProvider.createEngine(context, defaultSettings)
    }

    /**
     * The [Client] implementation (`concept-fetch`) used for HTTP requests.
     */
    val client: Client by lazy {
        EngineProvider.createClient(context)
    }

    /**
     * The [BrowserStore] holds the global [BrowserState].
     */
    val store by lazy {
        BrowserStore(
                middleware = listOf(
                        DownloadMiddleware(context, DownloadService::class.java),
                        ThumbnailsMiddleware(thumbnailStorage),
                        ReaderViewMiddleware(),
                        RegionMiddleware(
                                context,
                                LocationService.default()
                        ),
                        // SearchMiddleware(context, listOf("qwant")),
                        RecordingDevicesMiddleware(context)
                ) + EngineMiddleware.create(engine, ::findSessionById)
        )
    }

    private fun findSessionById(tabId: String): Session? {
        return sessionManager.findSessionById(tabId)
    }

    val sessionStorage: SessionStorage by lazy {
        SessionStorage(context, engine)
    }

    /**
     * The session manager component provides access to a centralized registry of
     * all browser sessions (i.e. tabs). It is initialized here to persist and restore
     * sessions from the [SessionStorage], and with a default session (about:blank) in
     * case all sessions/tabs are closed.
     */
    val sessionManager by lazy {
        SessionManager(engine, store).apply {
            // Install the "icons" WebExtension to automatically load icons for every visited website.
            icons.install(engine, store = store)

            // Show an ongoing notification when recording devices (camera, microphone) are used by web content
            //  RecordingDevicesNotificationFeature(context, sessionManager = this).enable()

            WebNotificationFeature(context, engine, icons, R.drawable.notification_icon, BrowserActivity::class.java)

            MediaSessionFeature(context, MediaSessionService::class.java, store).start()
        }
    }

    val customTabsStore by lazy { CustomTabsServiceStore() }

    /**
     * The storage component to persist browsing history (with the exception of
     * private sessions).
     */
    val historyStorage by lazy { History(context).apply {
        this.restore()
        this.setupAutoPersist(30000)
    } }

    /**
     * A storage component for persisting thumbnail images of tabs.
     */
    val thumbnailStorage by lazy { ThumbnailStorage(context) }

    /**
     * A storage component for site permissions.
     */
    val sitePermissionsStorage by lazy { SitePermissionsStorage(context) }

    /**
     * Icons component for loading, caching and processing website icons.
     */
    val icons by lazy { BrowserIcons(context, client) }

    // Addons
    val addonManager by lazy {
        val addonUpdater = DefaultAddonUpdater(context, AddonUpdater.Frequency(1, TimeUnit.DAYS))
        AddonManager(store, engine, addonCollectionProvider, addonUpdater)
    }

    val addonUpdater by lazy {
        DefaultAddonUpdater(context, AddonUpdater.Frequency(1, TimeUnit.DAYS))
    }


    val addonCollectionProvider by lazy {
        QwantAddonCollectionProvider(client)
    }

    /* private fun provideDefaultAddonCollectionProvider(): AddonCollectionProvider {
        return AddonCollectionProvider(
                context = context,
                client = client,
                collectionName = "7dfae8669acc4312a65e8ba5553036",
                maxCacheAgeInMinutes = DAY_IN_MINUTES
        )
    } */

    /**
     * Component for managing shortcuts (both regular and PWA).
     */
    val shortcutManager by lazy { WebAppShortcutManager(context, client, ManifestStorage(context)) }

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param prefs the shared preferences to use when reading tracking
     * protection settings.
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    fun createTrackingProtectionPolicy(
        prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        normalMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_normal), true),
        privateMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_private), true)
    ): TrackingProtectionPolicy {
        val trackingPolicy = TrackingProtectionPolicy.recommended()
        return when {
            normalMode && privateMode -> trackingPolicy
            normalMode && !privateMode -> trackingPolicy.forRegularSessionsOnly()
            !normalMode && privateMode -> trackingPolicy.forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.none()
        }
    }
}
