package org.mozilla.reference.browser

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.RecentlyClosedAction
import mozilla.components.concept.engine.Engine
import org.mozilla.reference.browser.ext.components

class MainViewModel(val app: Application) : AndroidViewModel(app) {
    override fun onCleared() {
        /* Log.d("QWANT_BROWSER", "OnCleared mainViewModel")
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        if (prefs.getBoolean(app.resources.getString(R.string.pref_key_privacy_cleardata_on_close), false)) {
            runBlocking {
                Log.d("QWANT_BROWSER", "viewmodel Should clean")
                app.components.core.engine.clearData(Engine.BrowsingData.all(), null,
                        { Log.d("QWANT_BROWSER", "viewmodel Clean ok") },
                        { Log.d("QWANT_BROWSER", "viewmodel Clean failed") }
                )
                app.components.core.store.dispatch(EngineAction.PurgeHistoryAction)
                app.components.core.store.dispatch(RecentlyClosedAction.RemoveAllClosedTabAction)
                Log.d("QWANT_BROWSER", "history test")

                app.components.core.historyStorage.deleteEverything()
                Log.d("QWANT_BROWSER", "all done")
            }
        } else {
            Log.d("QWANT_BROWSER", "viewmodel no cleaning")
        } */

        super.onCleared()
    }
}