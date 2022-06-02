/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.SystemAction
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.ktx.android.content.runOnlyInMainProcess
import mozilla.components.support.rusthttp.RustHttpConfig
import mozilla.components.support.webextensions.WebExtensionSupport
import org.mozilla.reference.browser.ext.application
import java.util.concurrent.TimeUnit

open class BrowserApplication : Application(), Application.ActivityLifecycleCallbacks {
    val components by lazy { Components(this) }

    private var currentActivity: Activity? = null

    override fun onCreate() {

        super.onCreate()

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dayNightMode = prefs.getString(resources.getString(R.string.pref_key_general_dark_theme), "2")
        var darkTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        when (dayNightMode) {
            "0" -> darkTheme = AppCompatDelegate.MODE_NIGHT_NO
            "1" -> darkTheme = AppCompatDelegate.MODE_NIGHT_YES
            "2" -> darkTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(darkTheme)

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
        restoreBrowserState()

        WebExtensionSupport.initialize(
            runtime = components.core.engine,
            store = components.core.store,
            onNewTabOverride = { _, engineSession, url ->
                val tabId = components.useCases.tabsUseCases.addTab(
                        url = url,
                        selectTab = true,
                        engineSession = engineSession
                )
                tabId
            },
            onCloseTabOverride = { _, sessionId ->
                components.useCases.tabsUseCases.removeTab(sessionId)
            },
            onSelectTabOverride = { _, sessionId ->
                components.useCases.tabsUseCases.selectTab(sessionId)
            },
            onExtensionsLoaded = {}
        )
    }

    private fun restoreBrowserState() = GlobalScope.launch(Dispatchers.Main) {
        val store = components.core.store
        val sessionStorage = components.core.sessionStorage

        components.useCases.tabsUseCases.restore(sessionStorage).invokeOnCompletion {
            if (components.core.store.state.tabs.isEmpty()) {
                components.useCases.tabsUseCases.addTab(QwantUtils.getHomepage(applicationContext), selectTab = true)
            }
        }

        // if (currentActivity != null && currentActivity is BrowserActivity)
        //    (currentActivity as BrowserActivity).updateTabCount()

        // Now that we have restored our previous state (if there's one) let's setup auto saving the state while
        // the app is used.
        sessionStorage.autoSave(store)
                .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
                .whenGoingToBackground()
                .whenSessionsChange()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        runOnlyInMainProcess {
            // components.core.sessionManager.onLowMemory()
            components.core.store.dispatch(SystemAction.LowMemoryAction(level))
            components.core.icons.onTrimMemory(level)
        }
    }

    override fun onActivityResumed(activity: Activity) { this.currentActivity = activity }
    override fun onActivityStarted(activity: Activity) { this.currentActivity = activity }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { this.currentActivity = activity }
    override fun onActivityPaused(activity: Activity) { }
    override fun onActivityDestroyed(activity: Activity) { }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }
    override fun onActivityStopped(activity: Activity) { }
}

private fun setupLogging() {
    // We want the log messages of all builds to go to Android logcat
    Log.addSink(AndroidLogSink())
    // RustLog.enable()
}
