/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.util.Log
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.webcompat.WebCompatFeature
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.WebExtension

object EngineProvider {

    private var runtime: GeckoRuntime? = null

    @Synchronized
    private fun getOrCreateRuntime(context: Context): GeckoRuntime {
        if (runtime == null) {
            val builder = GeckoRuntimeSettings.Builder()

            builder.aboutConfigEnabled(false)
            builder.consoleOutput(false)
            builder.debugLogging(false)
            builder.remoteDebuggingEnabled(false)

            runtime = GeckoRuntime.create(context, builder.build())
        }

        return runtime!!
    }

    fun createEngine(context: Context, defaultSettings: DefaultSettings): Engine {
        val runtime = getOrCreateRuntime(context)

        Log.d("QWANT_BROWSER_EXTENSION", "engine creation")
        runtime.webExtensionController
            // .installBuiltIn("resource://android/assets/webext_trackingprotection/")
            .ensureBuiltIn("resource://android/assets/webext_trackingprotection/", "qwantprivacypilot-internal-beta-01@qwant.com")
            .accept({ extension: WebExtension? ->
                    Log.d("QWANT_BROWSER_EXTENSION", "Qwant Extension installed: $extension")
            }) { e: Throwable? ->
                Log.e("QWANT_BROWSER_EXTENSION","Error registering Qwant WebExtension", e)
            }

        return GeckoEngine(context, defaultSettings, runtime).also {
            WebCompatFeature.install(it)
            // QwantWebExtFeature.install(it)
        }
    }

    fun createClient(context: Context): Client {
        val runtime = getOrCreateRuntime(context)
        return GeckoViewFetchClient(context, runtime)
    }
}
