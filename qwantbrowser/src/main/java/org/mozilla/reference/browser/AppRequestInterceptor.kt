/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.util.Log
import mozilla.components.browser.errorpages.ErrorPages
import mozilla.components.browser.errorpages.ErrorType
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import org.mozilla.reference.browser.ext.components

class AppRequestInterceptor(private val context: Context) : RequestInterceptor {
    val uaBase = context.getString(R.string.qwant_base_useragent)
    val uaExt = uaBase + context.getString(R.string.qwant_useragent_ext)

    override fun onLoadRequest(
            engineSession: EngineSession,
            uri: String,
            lastUri: String?,
            hasUserGesture: Boolean,
            isSameDomain: Boolean,
            isRedirect: Boolean,
            isDirectNavigation: Boolean,
            isSubframeRequest: Boolean
    ): RequestInterceptor.InterceptionResponse? {
        val isFlight = uri.startsWith(context.getString(R.string.qwantflight_startwith_filter))
        if (uri.startsWith(context.getString(R.string.homepage_startwith_filter)) && !isFlight) {
            engineSession.settings.userAgentString = uaExt

            if (uri.indexOf("&qbc=1") == -1 && !uri.startsWith(context.getString(R.string.qwantmaps_result_startwith_filter))) {
                var searchStart = uri.indexOf("&q=")
                if (searchStart == -1) searchStart = uri.indexOf("?q=")
                if (searchStart != -1) {
                    var searchEnd = uri.indexOf('&', searchStart + 3)
                    if (searchEnd == -1) searchEnd = uri.length
                    val searchTerms = uri.substring(searchStart + 3, searchEnd)

                    val isMaps = uri.startsWith(context.getString(R.string.qwantmaps_startwith_filter))
                    val redirectUri = QwantUtils.getHomepage(context, query = searchTerms, maps = isMaps)

                    Log.d("QWANT_BROWSER_REDIRECT", "redirect $uri to $redirectUri")

                    return RequestInterceptor.InterceptionResponse.Url(redirectUri)
                }
            }
        } else if (engineSession.settings.userAgentString?.endsWith(" QwantMobile/4.2") == true) {
            engineSession.settings.userAgentString = uaBase
        }
        return context.components.services.appLinksInterceptor.onLoadRequest(
                engineSession, uri, lastUri, hasUserGesture, isSameDomain, isRedirect, isDirectNavigation,
                isSubframeRequest
        )
    }

    override fun onErrorRequest(
            session: EngineSession,
            errorType: ErrorType,
            uri: String?
    ): RequestInterceptor.ErrorResponse? {
        val errorPage = ErrorPages.createUrlEncodedErrorPage(context, errorType, uri)
        return RequestInterceptor.ErrorResponse(errorPage)
    }

    override fun interceptsAppInitiatedRequests() = false
}
