package org.mozilla.reference.browser

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import mozilla.components.concept.engine.Engine
import org.mozilla.reference.browser.ext.components
import java.util.*

class QwantUtils {
    companion object {
        private var client: String? = null

        fun getHomepage(
            context: Context,
            query: String? = null,
            widget: Boolean = false,
            interface_language: String? = null,
            search_language: String? = null,
            search_region: String? = null,
            adult_content: String? = null,
            news_on_home: Boolean? = null,
            favicon_on_serp: Boolean? = null,
            results_in_new_tab: Boolean? = null,
            dark_theme: String? = null,
            custom_color: String? = null,
            custom_character: String? = null,
            tiles: Boolean? = null,
            maps: Boolean = false
        ) : String {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            var client = getClient(context, prefs)
            if (widget) client += "-widget"

            val l = interface_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_interface), "en_GB")
            val r = search_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_search), "en")
            val sr = search_region ?: prefs.getString(context.getString(R.string.pref_key_general_region_search), "GB")
            val s = adult_content ?: prefs.getString(context.getString(R.string.pref_key_general_adultcontent), "0")
            val hc = news_on_home ?: prefs.getBoolean(context.getString(R.string.pref_key_general_newsonhome), true)
            val b = results_in_new_tab ?: prefs.getBoolean(context.getString(R.string.pref_key_general_resultsinnewtab), false)
            val si = favicon_on_serp ?: prefs.getBoolean(context.getString(R.string.pref_key_general_favicononserp), true)

            val c = custom_color ?: prefs.getString(context.getString(R.string.pref_key_general_custom_color), "blue")
            val ch = custom_character ?: prefs.getString(context.getString(R.string.pref_key_general_custom_character), "random")
            val hti = tiles ?: prefs.getBoolean(context.getString(R.string.pref_key_general_tiles), true)

            var theme = dark_theme ?: prefs.getString(context.getString(R.string.pref_key_general_dark_theme), "2")
            if (theme == "2") {
                theme = if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) "1" else "0"
            }

            // val localeSplit = sr.split("_")

            val builder = StringBuilder()
            builder.append(context.getString(R.string.homepage_base))
            if (maps) builder.append("maps/")
            // if (music) builder.append("music/search")
            builder.append("?client=").append(client)
                .append("&l=").append(l?.toLowerCase(Locale.getDefault()))
                .append("&sr=").append(sr)
                .append("&r=").append(r)
                .append("&s=").append(s)
                .append("&hc=").append(if (hc) "1" else "0")
                .append("&si=").append(if (si) "1" else "0")
                .append("&b=").append(if (b) "1" else "0")
                .append("&theme=").append(theme)
                .append("&c=").append(c)
                .append("&ch=").append(ch)
                .append("&hti=").append(if (hti) "1" else "0")

            if (BuildConfig.BUILD_TYPE == "bouygues") {
                val firstRequestKey = context.getString(R.string.pref_key_first_request)
                if (prefs.getBoolean(firstRequestKey, true)) {
                    prefs.edit().putBoolean(firstRequestKey, false).apply()
                    builder.append("&f=1")
                }
            }

            if (widget) builder.append("&widget=1")
            if (query != null) builder.append("&q=").append(query)

            builder.append("&qbc=1")

            return builder.toString()
        }

        private fun getClient(context: Context, prefs: SharedPreferences) : String? {
            if (client == null) {
                client = prefs.getString(context.getString(R.string.pref_key_saved_client), null)
                if (client == null) {
                    // No saved value. Falling back to apk definition, and save it.
                    client = context.getString(R.string.app_client_string)
                    prefs.edit().apply {
                        this.putString(context.getString(R.string.pref_key_saved_client), client)
                        this.apply()
                    }
                }
            }
            return client
        }

        fun refreshQwantPages(
                context: Context,
                interface_language: String? = null,
                search_language: String? = null,
                search_region: String? = null,
                adult_content: String? = null,
                news_on_home: Boolean? = null,
                favicon_on_serp: Boolean? = null,
                results_in_new_tab: Boolean? = null,
                dark_theme: String? = null,
                custom_color: String? = null,
                custom_character: String? = null,
                tiles: Boolean? = null
        ) {
            context.components.core.store.state.tabs.forEach {
                if (it.content.url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
                    var query: String? = null
                    if (it.content.url.contains("?q=") || it.content.url.contains("&q=")) {
                        query = it.content.url.split("?q=", "&q=")[1].split("&")[0]
                    }
                    val reloadPage = getHomepage(context,
                        query = query,
                        interface_language = interface_language,
                        search_language = search_language,
                        search_region = search_region,
                        adult_content = adult_content,
                        news_on_home = news_on_home,
                        favicon_on_serp = favicon_on_serp,
                        results_in_new_tab = results_in_new_tab,
                        dark_theme = dark_theme,
                        custom_color = custom_color,
                        custom_character = custom_character,
                        tiles = tiles
                    )
                    context.components.useCases.sessionUseCases.loadUrl.invoke(reloadPage, it.id)
                }
            }
        }

        // TODO move this somewhere else ?
        fun clearDataOnQuit(context: Context, success: () -> Unit, error: (Throwable) -> Unit) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val enabled = prefs.getBoolean(context.getString(R.string.pref_key_privacy_cleardata_on_close), false)
            if (enabled) {
                val browsingDataInt = prefs.getInt(context.getString(R.string.pref_key_cleardata_content_browsingdata), 0)
                val browsingData = Engine.BrowsingData.select(browsingDataInt)

                clearData(context,
                    history = prefs.getBoolean(context.getString(R.string.pref_key_cleardata_content_history), false),
                    tabs = prefs.getBoolean(context.getString(R.string.pref_key_cleardata_content_tabs), false),
                    privateTabs = prefs.getBoolean(context.getString(R.string.pref_key_cleardata_content_tabs_private), false),
                    browsingData = browsingData,
                    success, error
                )
            } else {
                error(Throwable(message = "disabled"))
            }
        }

        fun clearData(
                context: Context,
                history: Boolean,
                tabs: Boolean,
                privateTabs: Boolean,
                browsingData: Engine.BrowsingData?,
                success: (() -> Unit)?,
                error: ((Throwable) -> Unit)?
        ) {
            var clearHistoryJob: Job? = null

            if (history) {
                clearHistoryJob = MainScope().launch {
                    context.components.core.historyStorage.deleteEverything()
                }
            }

            if (tabs) {
                context.components.useCases.tabsUseCases.removeNormalTabs.invoke()
            }
            if (privateTabs) {
                context.components.useCases.tabsUseCases.removePrivateTabs.invoke()
            }

            val onBrowsingDataComplete: () -> Unit = {
                if (clearHistoryJob?.isActive == true) {
                    clearHistoryJob.invokeOnCompletion {
                        if (it != null) error(it)
                        else success?.invoke()
                    }
                } else {
                    success?.invoke()
                }
            }

            if (browsingData != null) {
                context.components.core.engine.clearData(browsingData, onSuccess = onBrowsingDataComplete, onError = error ?: {})
            } else {
                onBrowsingDataComplete()
            }
        }
    }
}