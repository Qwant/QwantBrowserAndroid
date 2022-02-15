package org.mozilla.reference.browser.browser.icons.utils

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.manifest.Size
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.org.json.asSequence
import mozilla.components.support.ktx.android.org.json.tryGetString

import org.mozilla.reference.browser.browser.icons.BrowserIcons

import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException

/**
 * [MessageHandler] implementation that receives messages from the icons web extensions and performs icon loads.
 */
internal class IconMessageHandler(
        private val store: BrowserStore,
        private val sessionId: String,
        private val private: Boolean,
        private val icons: BrowserIcons
) : MessageHandler {
    private val scope = CoroutineScope(Dispatchers.IO)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE) // This only exists so that we can wait in tests.
    internal var lastJob: Job? = null

    override fun onMessage(message: Any, source: EngineSession?): Any {
        if (message is JSONObject) {
            message.toIconRequest(private)?.let { loadRequest(it) }
        } else {
            throw IllegalStateException("Received unexpected message: $message")
        }

        // Needs to return something that is not null and not Unit:
        // https://github.com/mozilla-mobile/android-components/issues/2969
        return ""
    }

    private fun loadRequest(request: IconRequest) {
        lastJob = scope.launch {
            val icon = icons.loadIconAsync(request).await()

            store.dispatch(ContentAction.UpdateIconAction(sessionId, request.url, icon.bitmap))
        }
    }
}

private val typeMap: Map<String, IconRequest.Resource.Type> = mutableMapOf(
        "manifest" to IconRequest.Resource.Type.MANIFEST_ICON,
        "icon" to IconRequest.Resource.Type.FAVICON,
        "shortcut icon" to IconRequest.Resource.Type.FAVICON,
        "fluid-icon" to IconRequest.Resource.Type.FLUID_ICON,
        "apple-touch-icon" to IconRequest.Resource.Type.APPLE_TOUCH_ICON,
        "image_src" to IconRequest.Resource.Type.IMAGE_SRC,
        "apple-touch-icon image_src" to IconRequest.Resource.Type.APPLE_TOUCH_ICON,
        "apple-touch-icon-precomposed" to IconRequest.Resource.Type.APPLE_TOUCH_ICON,
        "og:image" to IconRequest.Resource.Type.OPENGRAPH,
        "og:image:url" to IconRequest.Resource.Type.OPENGRAPH,
        "og:image:secure_url" to IconRequest.Resource.Type.OPENGRAPH,
        "twitter:image" to IconRequest.Resource.Type.TWITTER,
        "msapplication-TileImage" to IconRequest.Resource.Type.MICROSOFT_TILE
)

private fun JSONArray?.toResourceSizes(): List<Size> {
    val array = this ?: return emptyList()

    return try {
        array.asSequence { i -> getString(i) }
                .mapNotNull { raw -> Size.parse(raw) }
                .toList()
    } catch (e: JSONException) {
        Logger.warn("Could not parse message from icons extensions", e)
        emptyList()
    } catch (e: NumberFormatException) {
        Logger.warn("Could not parse message from icons extensions", e)
        emptyList()
    }
}

private fun JSONObject.toIconResource(): IconRequest.Resource? {
    try {
        val url = getString("href")
        val type = typeMap[getString("type")] ?: return null
        val sizes = optJSONArray("sizes").toResourceSizes()
        val mimeType = tryGetString("mimeType")
        val maskable = optBoolean("maskable", false)

        return IconRequest.Resource(
                url = url,
                type = type,
                sizes = sizes,
                mimeType = if (mimeType.isNullOrEmpty()) null else mimeType,
                maskable = maskable
        )
    } catch (e: JSONException) {
        Logger.warn("Could not parse message from icons extensions", e)
        return null
    }
}

internal fun JSONArray.toIconResources(): List<IconRequest.Resource> {
    return asSequence { i -> getJSONObject(i) }
            .mapNotNull { it.toIconResource() }
            .toList()
}

internal fun JSONObject.toIconRequest(isPrivate: Boolean): IconRequest? {
    return try {
        val url = getString("url")

        IconRequest(url, isPrivate = isPrivate, resources = getJSONArray("icons").toIconResources())
    } catch (e: JSONException) {
        Logger.warn("Could not parse message from icons extensions", e)
        null
    }
}
