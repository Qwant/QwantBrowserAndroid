package org.mozilla.reference.browser

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager

class QwantUtils {
    companion object {
        fun getHomepage(
                context: Context,
                query: String? = null,
                widget: Boolean = false,
                interface_language: String? = null,
                search_language: String? = null,
                adult_content: String? = null,
                news_on_home: Boolean? = null
        ) : String {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val client = context.getString(R.string.browser_client)
            val l = interface_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_interface), "en_US")
            val sr = search_language ?: prefs.getString(context.getString(R.string.pref_key_general_language_search), "en_US")
            val s = adult_content ?: prefs.getString(context.getString(R.string.pref_key_general_adultcontent), "0")
            val hc = news_on_home ?: prefs.getBoolean(context.getString(R.string.pref_key_general_newsonhome), true)

            val localeSplit = sr.split("_")

            val builder = StringBuilder()
            builder.append(context.getString(R.string.homepage_base))
                .append("?client=").append(client)
                .append("&l=").append(l.toLowerCase())
                .append("&sr=").append(localeSplit[0].toLowerCase()).append("&r=").append(localeSplit[1])
                .append("&s=").append(s)
                .append("&hc=").append(if (hc) "1" else "0")
                // TODO
                // .append("&a=").append(enableSuggest)
                // .append("&b=").append(targetBlank)
                // .append("&t=").append(theme)

            if (widget) builder.append("&widget=1")
            if (query != null) builder.append("&q=").append(query)

            Log.d("QWANT_BROWSER", "get homepage: $builder")

            return builder.toString()
        }
    }
}