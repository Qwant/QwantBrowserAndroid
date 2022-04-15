/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.util.Log
import mozilla.components.concept.engine.webextension.WebExtensionRuntime

/**
 * Feature to enable Qwant tracking protection extension
 */
object QwantWebExtFeature {
    internal const val QWANTWEBEXT_EXTENSION_ID = "qwantprivacypilot-internal-beta-01@qwant.com"
    internal const val QWANTWEBEXT_EXTENSION_URL = "resource://android/assets/webext_trackingprotection/"

    /**
     * Installs the web extension in the runtime through the WebExtensionRuntime install method
     */
    fun install(runtime: WebExtensionRuntime) {
        /* runtime.installWebExtension(
            QWANTWEBEXT_EXTENSION_ID, QWANTWEBEXT_EXTENSION_URL,
            onSuccess = {
                Log.d("QWANT_BROWSER_EXTENSION", "Qwant Extension installed: ${it.getMetadata()}")
            },
            onError = { ext, throwable ->
                Log.e("QWANT_BROWSER_EXTENSION","Error registering Qwant WebExtension", throwable)
            }
        ) */
    }
}
