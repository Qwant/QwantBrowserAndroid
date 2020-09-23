/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.session.Session
import mozilla.components.browser.state.state.SessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.feature.pwa.intent.WebAppIntentProcessor
import org.mozilla.reference.browser.ext.components

class IntentReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent?.let { Intent(it) } ?: Intent()
        val utils = components.utils

        Log.e("QWANT_BROWSER", "Intent received with data: ${intent.data} and action ${intent.action}")

        if (intent.action == WebAppIntentProcessor.ACTION_VIEW_PWA || intent.action == OLD_BOOKMARK_ACTION) {
            intent.action = ACTION_VIEW
        }

        /* val sessionManager = components.core.sessionManager
        val url = intent.dataString
        if (!intent.dataString.isNullOrEmpty()) {
            val existingSession = sessionManager.sessions.find { it.url == url }
            if (existingSession != null) {
                sessionManager.select(existingSession)
            } else {
                components.useCases.sessionUseCases.loadUrl(url, createSession(url, private = false, source = SessionState.Source.ACTION_VIEW), EngineSession.LoadUrlFlags.external())
            }

            intent.setClassName(applicationContext, BrowserActivity::class.java.name)

            startActivity(intent)
            finish()
        } */

        MainScope().launch {
            utils.intentProcessors.any { it.process(intent) }
            // utils.intentProcessors.process(intent)

            intent.setClassName(applicationContext, BrowserActivity::class.java.name)

            startActivity(intent)
            finish()
        }
    }

    private fun createSession(url: String, private: Boolean = false, source: SessionState.Source): Session {
        return Session(url, private, source).also { components.core.sessionManager.add(it, selected = true) }
    }

    companion object {
        const val OLD_BOOKMARK_ACTION = "org.mozilla.gecko.BOOKMARK"
    }
}
