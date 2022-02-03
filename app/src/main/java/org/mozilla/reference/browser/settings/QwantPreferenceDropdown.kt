package org.mozilla.reference.browser.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceViewHolder
import mozilla.components.support.ktx.android.content.getColorFromAttr
import org.mozilla.reference.browser.R

class QwantPreferenceDropdown : DropDownPreference {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        val titleView = holder?.findViewById(android.R.id.title) as TextView?
        val summaryView = holder?.findViewById(android.R.id.summary) as TextView?
        val iconView = holder?.findViewById(android.R.id.icon) as ImageView?

        // holder?.itemView?.setBackgroundColor(context.getColorFromAttr(R.attr.qwant_color_background))
        holder?.itemView?.background = ContextCompat.getDrawable(context, R.drawable.qwant_ripple)

        val mainColor = context.getColorFromAttr(R.attr.qwant_color_main)
        titleView?.setTextColor(mainColor)
        summaryView?.setTextColor(mainColor)
        iconView?.setColorFilter(mainColor)
    }

    fun forceNotifyChange() {
        notifyChanged()
    }
}