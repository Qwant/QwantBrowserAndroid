/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import mozilla.components.support.ktx.android.content.getColorFromAttr
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R


class CustomCharacterPreference : Preference {
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
        this.layoutResource = R.layout.preference_custom_layout
        this.widgetLayoutResource = R.layout.preference_custom_character
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.let {
            val titleView = it.itemView.findViewById(android.R.id.title) as TextView?
            val summaryView = it.itemView.findViewById(android.R.id.summary) as TextView?
            val iconView = it.itemView.findViewById(android.R.id.icon) as ImageView?
            val mainColor = context.getColorFromAttr(R.attr.qwant_color_main)
            titleView?.setTextColor(mainColor)
            summaryView?.setTextColor(mainColor)
            iconView?.setColorFilter(mainColor)

            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = context.getString(R.string.pref_key_general_custom_character)

            val random = it.itemView.findViewById<ImageButton>(R.id.character_random)
            val puffa = it.itemView.findViewById<ImageButton>(R.id.character_1)
            val football = it.itemView.findViewById<ImageButton>(R.id.character_2)
            val turtleneck = it.itemView.findViewById<ImageButton>(R.id.character_3)
            val glasses = it.itemView.findViewById<ImageButton>(R.id.character_4)
            val cat = it.itemView.findViewById<ImageButton>(R.id.character_5)
            val balloon = it.itemView.findViewById<ImageButton>(R.id.character_6)
            val none = it.itemView.findViewById<ImageButton>(R.id.character_none)
            val allButtons = listOf(random, puffa, football, turtleneck, glasses, cat, balloon, none)

            val currentValue = prefs.getString(prefKey, "random")
            summaryView?.text = when (currentValue) {
                "random" -> {
                    random?.isSelected = true
                    context.getString(R.string.character_random)
                }
                "puffa" -> {
                    puffa?.isSelected = true
                    context.getString(R.string.character_doudoune)
                }
                "football" -> {
                    football?.isSelected = true
                    context.getString(R.string.character_football)
                }
                "turtleneck" -> {
                    turtleneck?.isSelected = true
                    context.getString(R.string.character_turtleneck)
                }
                "glasses" -> {
                    glasses?.isSelected = true
                    context.getString(R.string.character_glasses)
                }
                "cat" -> {
                    cat?.isSelected = true
                    context.getString(R.string.character_cat)
                }
                "balloon" -> {
                    balloon?.isSelected = true
                    context.getString(R.string.character_balloon)
                }
                "none" -> {
                    none?.isSelected = true
                    context.getString(R.string.character_none)
                }
                else -> ""
            }


            random?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons,"random", R.string.character_random
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_1)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "puffa", R.string.character_doudoune
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_2)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "football", R.string.character_football
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_3)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "turtleneck", R.string.character_turtleneck
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_4)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "glasses", R.string.character_glasses
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_5)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "cat", R.string.character_cat
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_6)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "balloon", R.string.character_balloon
            ))
            it.itemView.findViewById<ImageButton>(R.id.character_none)?.setOnClickListener(OnCharacterClickListener(
                context, summaryView, allButtons, "none", R.string.character_none
            ))
        }
    }

    internal class OnCharacterClickListener(
        val context: Context,
        val summaryView: TextView?,
        val allButtons: List<ImageButton>,
        val value: String,
        val nameId: Int
    ): OnClickListener {
        private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private val prefKey = context.getString(R.string.pref_key_general_custom_character)

        override fun onClick(v: View?) {
            allButtons.forEach { it.isSelected = false }
            v?.isSelected = true
            summaryView?.text = context.getString(nameId)
            prefs.edit().putString(prefKey, value).apply()
            QwantUtils.refreshQwantPages(context, custom_character = value)
        }
    }
}
