package org.mozilla.reference.browser

import android.content.Context
import mozilla.components.browser.errorpages.ErrorPages
import mozilla.components.browser.errorpages.ErrorType
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import org.mozilla.reference.browser.ext.components

class AppRequestInterceptor(private val context: Context) : RequestInterceptor {
    override fun onLoadRequest(session: EngineSession, uri: String): RequestInterceptor.InterceptionResponse? {
        return context.components.firefoxAccountsIntegration.interceptor.onLoadRequest(session, uri)
    }

    override fun onErrorRequest(
        session: EngineSession,
        errorType: ErrorType,
        uri: String?
    ): RequestInterceptor.ErrorResponse? {
        return RequestInterceptor.ErrorResponse(ErrorPages.createErrorPage(context, errorType))
    }
}
