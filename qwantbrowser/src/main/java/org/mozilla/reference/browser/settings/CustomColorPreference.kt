/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import mozilla.components.support.ktx.android.content.getColorFromAttr
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R


class CustomColorPreference : Preference {

    private val blueBackground = ContextCompat.getDrawable(context, R.drawable.round_background_preference_selection)?.let {
        DrawableCompat.setTint(it, context.resources.getColor(R.color.qwant_blue_v2))
        it
    }
    private val greenBackground = ContextCompat.getDrawable(context, R.drawable.round_background_preference_selection)?.let {
        DrawableCompat.setTint(it, context.resources.getColor(R.color.qwant_green_v2))
        it
    }
    private val pinkBackground = ContextCompat.getDrawable(context, R.drawable.round_background_preference_selection)?.let {
        DrawableCompat.setTint(it, context.resources.getColor(R.color.qwant_pink_100))
        it
    }
    private val purpleBackground = ContextCompat.getDrawable(context, R.drawable.round_background_preference_selection)?.let {
        DrawableCompat.setTint(it, context.resources.getColor(R.color.qwant_purple_200))
        it
    }
    private val checkDrawable = ContextCompat.getDrawable(context, R.drawable.icons_regular_icon_check)?.let {
        DrawableCompat.setTint(it, context.resources.getColor(R.color.qwant_black_v2))
        it
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefKey = context.getString(R.string.pref_key_general_custom_color)

    private var selected = prefs.getString(prefKey, "blue")

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        this.widgetLayoutResource = R.layout.preference_custom_color
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.let { h ->
            val titleView = h.itemView.findViewById(android.R.id.title) as TextView?
            val summaryView = h.itemView.findViewById(android.R.id.summary) as TextView?
            val iconView = h.itemView.findViewById(android.R.id.icon) as ImageView?
            val mainColor = context.getColorFromAttr(R.attr.qwant_color_main)
            titleView?.setTextColor(mainColor)
            summaryView?.setTextColor(mainColor)
            iconView?.setColorFilter(mainColor)

            val blueButton = h.itemView.findViewById<ImageButton>(R.id.theme_blue)
            val pinkButton = h.itemView.findViewById<ImageButton>(R.id.theme_pink)
            val greenButton = h.itemView.findViewById<ImageButton>(R.id.theme_green)
            val purpleButton = h.itemView.findViewById<ImageButton>(R.id.theme_purple)

            blueButton.background = blueBackground
            blueButton.setOnClickListener {
                disableInvalidCheck(blueButton, greenButton, pinkButton, purpleButton) // Do this before changing selected !
                selected = "blue"
                blueButton.setImageDrawable(checkDrawable)
                summaryView?.text = context.getString(R.string.custom_color_blue)
                prefs.edit().putString(prefKey, "blue").apply()
                QwantUtils.refreshQwantPages(context, custom_color = "blue")
            }

            greenButton.background = greenBackground
            greenButton.setOnClickListener {
                disableInvalidCheck(blueButton, greenButton, pinkButton, purpleButton)
                selected = "green"
                greenButton.setImageDrawable(checkDrawable)
                summaryView?.text = context.getString(R.string.custom_color_green)
                prefs.edit().putString(prefKey, "green").apply()
                QwantUtils.refreshQwantPages(context, custom_color = "green")
            }

            pinkButton.background = pinkBackground
            pinkButton.setOnClickListener {
                disableInvalidCheck(blueButton, greenButton, pinkButton, purpleButton)
                selected = "pink"
                pinkButton.setImageDrawable(checkDrawable)
                summaryView?.text = context.getString(R.string.custom_color_pink)
                prefs.edit().putString(prefKey, "pink").apply()
                QwantUtils.refreshQwantPages(context, custom_color = "pink")
            }

            purpleButton.background = purpleBackground
            purpleButton.setOnClickListener {
                disableInvalidCheck(blueButton, greenButton, pinkButton, purpleButton)
                selected = "purple"
                purpleButton.setImageDrawable(checkDrawable)
                summaryView?.text = context.getString(R.string.custom_color_purple)
                prefs.edit().putString(prefKey, "purple").apply()
                QwantUtils.refreshQwantPages(context, custom_color = "purple")
            }

            when (selected) {
                "blue" -> blueButton.setImageDrawable(checkDrawable)
                "green" -> greenButton.setImageDrawable(checkDrawable)
                "pink" -> pinkButton.setImageDrawable(checkDrawable)
                "purple" -> purpleButton.setImageDrawable(checkDrawable)
            }
        }
    }

    fun disableInvalidCheck(b: ImageButton, g: ImageButton, pi: ImageButton, pu: ImageButton) {
        when (selected) {
            "blue" -> b.setImageDrawable(null)
            "green" -> g.setImageDrawable(null)
            "pink" -> pi.setImageDrawable(null)
            "purple" -> pu.setImageDrawable(null)
        }
    }
}
