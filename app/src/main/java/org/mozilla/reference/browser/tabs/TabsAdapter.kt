package org.mozilla.reference.browser.tabs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.tabstray.Tab
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.tabs.tray.toTab

class TabsAdapter(
        private val context: Context,
        private var isPrivate: Boolean,
        private val selectedCallback: (tab: Tab?) -> Unit,
        private val deletedCallback: (tabSession: TabSessionState?) -> Unit
)  : BaseAdapter() {
    var tabs = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate)

    internal class TabListItemViewHolder(
            item_layout: View,
            private val context: Context,
            private val selectedCallback: (tab: Tab?) -> Unit,
            private val deletedCallback: (tabSession: TabSessionState?) -> Unit
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemMainLayout: LinearLayout = item_layout.findViewById(R.id.tablist_item_layout)
        private var itemIcon: ImageView = item_layout.findViewById(R.id.tablist_item_icon)
        private var itemPreview: TopCropImageView = item_layout.findViewById(R.id.tablist_item_preview)
        private var itemTitle: TextView = item_layout.findViewById(R.id.tablist_item_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.tablist_item_url)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.tablist_item_layout_text)
        private var itemDelete: ImageButton = item_layout.findViewById(R.id.tablist_item_delete)

        fun setup(tabSession: TabSessionState, isSelected: Boolean) {
            val tab = tabSession.toTab()

            val title = if (tab.title.length > MAX_TITLE_LENGTH) tab.title.substring(0, MAX_TITLE_LENGTH - 3) + "..." else tab.title
            val url = if (tab.url.length > MAX_URL_LENGTH) tab.url.substring(0, MAX_URL_LENGTH - 3) + "..." else tab.url

            this.itemTitle.text = title
            this.itemUrl.text = url
            this.itemLayoutText.setOnClickListener { selectedCallback.invoke(tab) }
            this.itemDelete.setOnClickListener { deletedCallback.invoke(tabSession) }

            if (tab.thumbnail != null) {
                this.itemPreview.visibility = View.VISIBLE
                this.itemIcon.visibility = View.GONE
                this.itemPreview.setImageBitmap(tab.thumbnail)
            } else {
                this.itemPreview.visibility = View.GONE
                this.itemIcon.visibility = View.VISIBLE
                if (tab.icon != null) this.itemIcon.setImageBitmap(tab.icon)
            }

            this.setSelected(isSelected)
        }

        fun setSelected(isSelected: Boolean) {
            if (isSelected) {
                this.itemMainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.qwant_tab_selected))
            } else {
                this.itemMainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.qwant_background))
            }
        }

        companion object {
            private const val MAX_TITLE_LENGTH = 45
            private const val MAX_URL_LENGTH = 40
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View
        val viewHolder: TabListItemViewHolder

        val tabSession = this.getItem(position) as TabSessionState
        val selectedState = (context.components.core.store.state.selectedTabId == tabSession.id)

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.tablist_item, parent, false)
            viewHolder = TabListItemViewHolder(newView, context, selectedCallback, deletedCallback)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as TabListItemViewHolder
            newView = convertView
        }

        if (position < tabs.count()) {
            viewHolder.setup(tabSession, selectedState)
        }
        viewHolder.setSelected(selectedState)

        return newView
    }

    override fun getItem(position: Int): Any {
        return tabs[tabs.count() - 1 - position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return tabs.count()
    }

    fun tabChanged(/* isPrivate: Boolean */) {
        // this.isPrivate = isPrivate
        tabs = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate)
        this.notifyDataSetChanged()
    }
}