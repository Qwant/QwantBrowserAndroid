package org.mozilla.reference.browser

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import java.util.*

class QwantUtils {
    companion object {
        fun getHomepage(
                context: Context,
                query: String? = null,
                widget: Boolean = false,
                interface_language: String? = null,
                search_language: String? = null,
                search_region: String? = null,
                adult_content: String? = null,
                news_on_home: Boolean? = null,
                results_in_new_tab: Boolean? = null,
                dark_theme: String? = null
        ) : String {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val client = context.getString(R.string.browser_client)
            val l = interface_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_interface), "en_GB")
            val r = search_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_search), "en")
            val sr = search_region ?: prefs.getString(context.getString(R.string.pref_key_general_region_search), "GB")
            val s = adult_content ?: prefs.getString(context.getString(R.string.pref_key_general_adultcontent), "2")
            val hc = news_on_home ?: prefs.getBoolean(context.getString(R.string.pref_key_general_newsonhome), false)
            val b = results_in_new_tab ?: prefs.getBoolean(context.getString(R.string.pref_key_general_resultsinnewtab), false)

            var theme = dark_theme ?: prefs.getString(context.getString(R.string.pref_key_general_dark_theme), "2")
            if (theme == "2") {
                theme = if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) "1" else "0"
            }

            // val localeSplit = sr.split("_")

            val builder = StringBuilder()
            builder.append(context.getString(R.string.homepage_base))
                .append("?client=").append(client)
                .append("&l=").append(l?.toLowerCase(Locale.getDefault()))
                .append("&sr=").append(sr)
                .append("&r=").append(r)
                .append("&s=").append(s)
                .append("&hc=").append(if (hc) "1" else "0")
                .append("&b=").append(if (b) "1" else "0")
                .append("&theme=").append(theme)
                // TODO
                // .append("&a=").append(enableSuggest)

            if (widget) builder.append("&widget=1")
            if (query != null) builder.append("&q=").append(query)

            builder.append("&qbc=1")

            return builder.toString()
        }
    }
}