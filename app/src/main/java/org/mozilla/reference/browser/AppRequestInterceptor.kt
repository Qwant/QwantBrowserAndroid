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
import org.mozilla.reference.browser.tabs.PrivatePage

class AppRequestInterceptor(private val context: Context) : RequestInterceptor {
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
