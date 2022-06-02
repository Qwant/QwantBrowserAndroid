/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.qwant.android.webext

import android.util.Log
import mozilla.components.concept.engine.webextension.WebExtensionRuntime

/**
 * Feature to enable Qwant tracking protection extension
 */
object QwantWebExtFeature {
    private const val QWANTWEBEXT_EXTENSION_ID = "qwant-vip-android@qwant.com"
    private const val QWANTWEBEXT_EXTENSION_URL = "resource://android/assets/qwant_webext/"

    /**
     * Installs the web extension in the runtime through the WebExtensionRuntime install method
     */
    fun install(runtime: WebExtensionRuntime) {
        runtime.listInstalledWebExtensions({ list ->
            Log.d("QWANT_BROWSER_EXTENSION", "Extension count installed at start: ${list.size}")
            var extensionFound = false
            list.forEach { ext ->
                Log.d("QWANT_BROWSER_EXTENSION", "Extension found: ${ext.id} - ${ext.getMetadata()}")
                if (ext.id == QWANTWEBEXT_EXTENSION_ID) {
                    extensionFound = true
                    /* Log.d("QWANT_BROWSER_EXTENSION", "Qwant Extension found: should try updating")
                    runtime.updateWebExtension(ext,
                        onSuccess = {
                            Log.d("QWANT_BROWSER_EXTENSION", "Qwant webextension updated: ${it?.getMetadata()}")
                        },
                        onError = { _, throwable ->
                            Log.e("QWANT_BROWSER_EXTENSION", "Error updating Qwant webextension", throwable)
                        }
                    ) */
                }
            }
            if (!extensionFound) {
                Log.d("QWANT_BROWSER_EXTENSION", "Qwant extension not found. installing")
                runtime.installWebExtension(
                    QWANTWEBEXT_EXTENSION_ID, QWANTWEBEXT_EXTENSION_URL,
                    onSuccess = { ext ->
                        Log.d("QWANT_BROWSER_EXTENSION", "Qwant Extension installed: ${ext.getMetadata()}")
                    },
                    onError = { _, throwable ->
                        Log.e("QWANT_BROWSER_EXTENSION","Error registering Qwant WebExtension", throwable)
                    }
                )
            }
        })

    }
}
