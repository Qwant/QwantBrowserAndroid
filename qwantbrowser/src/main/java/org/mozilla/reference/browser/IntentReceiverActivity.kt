/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.feature.pwa.intent.WebAppIntentProcessor
import org.mozilla.reference.browser.ext.components

class IntentReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent?.let { Intent(it) } ?: Intent()
        val utils = components.utils

        if (intent.action == WebAppIntentProcessor.ACTION_VIEW_PWA || intent.action == OLD_BOOKMARK_ACTION) {
            intent.action = ACTION_VIEW
        }

        MainScope().launch {
            utils.intentProcessors.any { it.process(intent) }

            intent.setClassName(applicationContext, BrowserActivity::class.java.name)

            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val OLD_BOOKMARK_ACTION = "org.mozilla.gecko.BOOKMARK"
    }
}
