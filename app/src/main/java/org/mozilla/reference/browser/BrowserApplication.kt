/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.Nullable
import mozilla.components.browser.session.Session
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.ktx.android.content.runOnlyInMainProcess
import mozilla.components.support.rustlog.RustLog
import mozilla.components.support.rusthttp.RustHttpConfig
import mozilla.components.support.webextensions.WebExtensionSupport
import org.mozilla.reference.browser.ext.application

open class BrowserApplication : Application(), Application.ActivityLifecycleCallbacks {
    val components by lazy { Components(this) }

    var currentActivity: Activity? = null

    override fun onCreate() {

        super.onCreate()

        application.registerActivityLifecycleCallbacks(this)

        RustHttpConfig.setClient(lazy { components.core.client })
        setupLogging()

        if (!isMainProcess()) {
            // If this is not the main process then do not continue with the initialization here. Everything that
            // follows only needs to be done in our app's main process and should not be done in other processes like
            // a GeckoView child process or the crash handling process. Most importantly we never want to end up in a
            // situation where we create a GeckoRuntime from the Gecko child process (
            return
        }

        components.core.engine.warmUp()

        WebExtensionSupport.initialize(
            engine = components.core.engine,
            store = components.core.store,
            onNewTabOverride = { _, engineSession, url ->
                val session = Session(url)
                components.core.sessionManager.add(session, true, engineSession)
                session.id
            },
            onCloseTabOverride = { _, sessionId ->
                components.useCases.tabsUseCases.removeTab(sessionId)
            },
            onSelectTabOverride = { _, sessionId ->
                val selected = components.core.sessionManager.findSessionById(sessionId)
                selected?.let { components.useCases.tabsUseCases.selectTab(it) }
            }
        )
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        runOnlyInMainProcess {
            components.core.sessionManager.onLowMemory()
        }
    }

    override fun onActivityResumed(activity: Activity?) { this.currentActivity = activity }
    override fun onActivityStarted(activity: Activity?) { this.currentActivity = activity }
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) { this.currentActivity = activity }
    override fun onActivityPaused(activity: Activity?) { }
    override fun onActivityDestroyed(activity: Activity?) { }
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) { }
    override fun onActivityStopped(activity: Activity?) { }
}

private fun setupLogging() {
    // We want the log messages of all builds to go to Android logcat
    Log.addSink(AndroidLogSink())
    RustLog.enable()
}
