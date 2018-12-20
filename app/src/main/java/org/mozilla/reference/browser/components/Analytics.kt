/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.service.MozillaSocorroService
import mozilla.components.lib.crash.service.SentryService
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.BuildConfig
import org.mozilla.reference.browser.R

/**
 * Component group for all functionality related to analytics e.g. crash
 * reporting and telemetry.
 */
class Analytics(private val context: Context) {

    /**
     * A generic crash reporter component configured to use both Sentry and Socorro.
     */
    val crashReporter: CrashReporter by lazy {
        val sentryService = SentryService(
            context,
            BuildConfig.SENTRY_TOKEN,
            sendEventForNativeCrashes = true
        )

        val socorroService = MozillaSocorroService(context, "ReferenceBrowser")

        CrashReporter(
            services = listOf(sentryService, socorroService),
            shouldPrompt = CrashReporter.Prompt.ALWAYS,
            promptConfiguration = CrashReporter.PromptConfiguration(
                appName = context.getString(R.string.app_name),
                organizationName = "Mozilla"
            ),
            nonFatalCrashIntent = PendingIntent
                .getBroadcast(context, 0, Intent(BrowserApplication.NON_FATAL_CRASH_BROADCAST), 0),
            enabled = true
        )
    }
}
