/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.util.Log
import mozilla.components.browser.errorpages.ErrorType
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor

class AppRequestInterceptor(private val context: Context) : RequestInterceptor {
    /* override fun onLoadRequest(engineSession: EngineSession, uri: String, hasUserGesture: Boolean, isSameDomain: Boolean, isRedirect: Boolean, isDirectNavigation: Boolean): RequestInterceptor.InterceptionResponse? {
        Log.d("QWANT_BROWSER", "INTERCEPT: ${engineSession.settings.userAgentString}")
        /* if (!engineSession.settings.userAgentString!!.contains("QwantMobile")) {
            Log.d("QWANT_BROWSER", "rewrite ua for: $uri")
            engineSession.settings.userAgentString += " INTERCEPT"
        } */
        engineSession.settings.userAgentString += " INTERCEPT"
        return super.onLoadRequest(engineSession, uri, hasUserGesture, isSameDomain, isRedirect, isDirectNavigation)
    } */

    override fun onErrorRequest(
        session: EngineSession,
        errorType: ErrorType,
        uri: String?
    ): RequestInterceptor.ErrorResponse? {
        // return RequestInterceptor.ErrorResponse(ErrorPages.createErrorPage(context, errorType))
        // return RequestInterceptor.ErrorResponse("test")
        return null
    }
}
