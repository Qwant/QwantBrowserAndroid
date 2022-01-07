package org.mozilla.reference.browser.downloads

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.fetch.Headers
import mozilla.components.concept.fetch.MutableHeaders
import mozilla.components.concept.fetch.Request
import mozilla.components.concept.fetch.Response
import mozilla.components.concept.fetch.isDataUri
import mozilla.components.concept.fetch.isBlobUri
import mozilla.components.concept.fetch.Response.Companion.SUCCESS

import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoWebExecutor
import org.mozilla.geckoview.WebRequest
import org.mozilla.geckoview.WebRequest.CACHE_MODE_DEFAULT
import org.mozilla.geckoview.WebRequest.CACHE_MODE_RELOAD
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * GeckoView ([GeckoWebExecutor]) based implementation of [Client].
 */
class GeckoViewFetchClient(
        context: Context,
        runtime: GeckoRuntime = GeckoRuntime.getDefault(context),
        private val maxReadTimeOut: Pair<Long, TimeUnit> = Pair(MAX_READ_TIMEOUT_MINUTES, TimeUnit.MINUTES)
) : Client() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var executor: GeckoWebExecutor = GeckoWebExecutor(runtime)

    @Throws(IOException::class)
    override fun fetch(request: Request): Response {
        Log.e("QWANT_BROWSER", "fetch start")
        if (request.isDataUri()) {
            Log.e("QWANT_BROWSER", "fetch 1")
            return fetchDataUri(request)
        }

        Log.e("QWANT_BROWSER", "fetch 2")

        val webRequest = request.toWebRequest()

        Log.e("QWANT_BROWSER", "fetch 3")

        val readTimeOut = request.readTimeout ?: maxReadTimeOut
        val readTimeOutMillis = readTimeOut.let { (timeout, unit) ->
            unit.toMillis(timeout)
        }

        Log.e("QWANT_BROWSER", "fetch 4")

        return try {
            Log.e("QWANT_BROWSER", "fetch 5")

            var fetchFlags = 0
            if (request.cookiePolicy == Request.CookiePolicy.OMIT) {
                Log.e("QWANT_BROWSER", "fetch 6")
                fetchFlags += GeckoWebExecutor.FETCH_FLAGS_ANONYMOUS
            }
            if (request.redirect == Request.Redirect.MANUAL) {
                Log.e("QWANT_BROWSER", "fetch 7")
                fetchFlags += GeckoWebExecutor.FETCH_FLAGS_NO_REDIRECTS
            }
            Log.e("QWANT_BROWSER", "fetch 8")
            val webResponse = executor.fetch(webRequest, fetchFlags).poll(readTimeOutMillis)
            Log.e("QWANT_BROWSER", "fetch 9")
            webResponse?.toResponse(request.isBlobUri()) ?: throw IOException("Fetch failed with null response")
        } catch (e: TimeoutException) {
            Log.e("QWANT_BROWSER", "fetch 10")
            throw SocketTimeoutException()
        } catch (e: WebRequestError) {
            throw IOException(e)
        }
    }

    companion object {
        const val MAX_READ_TIMEOUT_MINUTES = 5L
    }
}

private fun Request.toWebRequest(): WebRequest = WebRequest.Builder(url)
        .method(method.name)
        .addHeadersFrom(this)
        .addBodyFrom(this)
        .cacheMode(if (useCaches) CACHE_MODE_DEFAULT else CACHE_MODE_RELOAD)
        .build()

private fun WebRequest.Builder.addHeadersFrom(request: Request): WebRequest.Builder {
    request.headers?.forEach { header ->
        addHeader(header.name, header.value)
    }

    return this
}

private fun WebRequest.Builder.addBodyFrom(request: Request): WebRequest.Builder {
    request.body?.let { body ->
        body.useStream { inStream ->
            val bytes = inStream.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            this.body(buffer)
        }
    }

    return this
}

@VisibleForTesting
internal fun WebResponse.toResponse(isBlobUri: Boolean): Response {
    val headers = translateHeaders(this)
    // We use the same API for blobs and HTTP requests, but blobs won't receive a status code.
    // If no exception is thrown we assume success.
    val status = if (isBlobUri) SUCCESS else statusCode
    return Response(
            uri,
            status,
            headers,
            body?.let {
                Response.Body(it, headers["Content-Type"])
            } ?: Response.Body.empty()
    )
}

private fun translateHeaders(webResponse: WebResponse): Headers {
    val headers = MutableHeaders()
    webResponse.headers.forEach { (k, v) ->
        v.split(",").forEach { headers.append(k, it.trim()) }
    }

    return headers
}
