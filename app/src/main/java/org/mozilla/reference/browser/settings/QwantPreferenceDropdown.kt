package org.mozilla.reference.browser.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
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

        val titleView = holder?.findViewById(android.R.id.title) as TextView
        titleView.setTextColor(context.getColorFromAttr(R.attr.qwant_color_main))
    }

    fun forceNotifyChange() {
        notifyChanged()
    }
}