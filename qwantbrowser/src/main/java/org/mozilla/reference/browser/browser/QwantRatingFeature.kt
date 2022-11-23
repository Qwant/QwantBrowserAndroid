package org.mozilla.reference.browser.browser

/* import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import org.mozilla.reference.browser.BrowserActivity.Companion.RATEPUSH_SEARCH_LIMIT
import org.mozilla.reference.browser.R

@ExperimentalCoroutinesApi
class QwantRatingFeature(val context: Context, val store: BrowserStore,) : LifecycleAwareFeature {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefsEditor = prefs.edit()
    private val searchesKey = context.getString(R.string.pref_key_ratepush_searches)
    private val searches: MutableSet<String> = prefs.getStringSet(searchesKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    private var checkSearchesForRating: Boolean = (searches.size < RATEPUSH_SEARCH_LIMIT && prefs.getLong(context.getString(R.string.pref_key_ratepush_date), 0) == 0L)

    private var urlChangedScope: CoroutineScope? = null

    override fun start() {
        if (checkSearchesForRating) {
            Log.d("QWANT_BROWSER_RATING", "Launching URL scope observer")
            urlChangedScope = store.flowScoped { flow -> flow
                .ifAnyChanged { arrayOf(it.selectedTabId, it.selectedTab?.content?.url)}
                .collect { state ->
                    Log.d("QWANT_BROWSER_RATING", "URL scope observer: url changed or tab switched")

                    val uri = state.selectedTab?.content?.url ?: ""

                    if (uri.startsWith(context.getString(R.string.homepage_startwith_filter))) {
                        if (!uri.startsWith(context.getString(R.string.qwantmaps_result_startwith_filter))) {
                            var searchStart = uri.indexOf("&q=")
                            if (searchStart == -1) searchStart = uri.indexOf("?q=")
                            if (searchStart != -1) {
                                var searchEnd = uri.indexOf('&', searchStart + 3)
                                if (searchEnd == -1) searchEnd = uri.length
                                val searchTerms = uri.substring(searchStart + 3, searchEnd)

                                val isMaps = uri.startsWith(context.getString(R.string.qwantmaps_startwith_filter))
                                val isMusic = uri.startsWith(context.getString(R.string.qwantmusic_startwith_filter))

                                if (!isMaps && !isMusic && !searches.contains(searchTerms)) {
                                    Log.d("QWANT_BROWSER_RATING", "New search recorded: $searchTerms | total: ${searches.size}")
                                    searches.add(searchTerms)

                                    prefsEditor.putStringSet(searchesKey, searches)
                                    prefsEditor.commit()

                                    if (searches.size >= RATEPUSH_SEARCH_LIMIT) {
                                        Log.d("QWANT_BROWSER_RATING", "URL scope observer target OK. Removing observer")
                                        checkSearchesForRating = false
                                        urlChangedScope?.cancel()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Log.d("QWANT_BROWSER_RATING", "URL scope observer not launched because already OK")
        }
    }

    override fun stop() {
        urlChangedScope?.cancel()
    }
} */