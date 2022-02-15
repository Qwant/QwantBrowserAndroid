package org.mozilla.reference.browser.tabs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.tabstray.thumbnail.TabThumbnailView
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.concept.tabstray.Tab
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import java.net.URI
import java.net.URISyntaxException

class TabsAdapter(
        private val context: Context,
        private val selectedCallback: (tab: Tab?) -> Unit
)  : BaseAdapter() {
    var tabs: Tabs? = null

    internal class TabListItemViewHolder(
            item_layout: View,
            private val context: Context,
            private val selectedCallback: (tab: Tab?) -> Unit,
            private val deletedCallback: (tab: Tab?) -> Unit
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemMainLayout: LinearLayout = item_layout.findViewById(R.id.tablist_item_layout)
        private var itemPreview: TabThumbnailView = item_layout.findViewById(R.id.tablist_item_preview)
        private var itemTitle: TextView = item_layout.findViewById(R.id.tablist_item_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.tablist_item_url)
        private var itemDelete: AppCompatImageButton = item_layout.findViewById(R.id.tablist_item_delete)

        private val thumbnailLoader = ThumbnailLoader(context.components.core.thumbnailStorage)

        fun setup(tab: Tab, isSelected: Boolean) {
            val title = if (tab.title.length > MAX_TITLE_LENGTH) tab.title.substring(0, MAX_TITLE_LENGTH - 3) + "..." else tab.title

            val host: String = try {
                URI(tab.url).host ?: tab.url
            } catch (e: URISyntaxException) {
                tab.url
            }

            this.itemTitle.text = title
            this.itemUrl.text = if (host.startsWith("www.")) host.substring(4) else host
            this.itemMainLayout.setOnClickListener { selectedCallback.invoke(tab) }
            this.itemDelete.setOnClickListener { deletedCallback.invoke(tab) }

            this.setThumbnail(tab)
            this.setSelected(isSelected)
        }

        private fun setSelected(isSelected: Boolean) {
            if (isSelected) {
                this.itemMainLayout.setBackgroundColor(context.getColorFromAttr(R.attr.qwant_tabs_selectedColor))
            } else {
                this.itemMainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.qwant_background))
            }
        }

        private fun setThumbnail(tab: Tab) {
            if (tab.thumbnail == null) {
                val thumbnailSize = 100.dpToPx(this.itemPreview.context.resources.displayMetrics)
                thumbnailLoader.loadIntoView(
                    this.itemPreview,
                    ImageLoadRequest(id = tab.id, size = thumbnailSize)
                )
            } else if (tab.thumbnail != null) {
                this.itemPreview.setImageBitmap(tab.thumbnail)
            }
        }

        companion object {
            private const val MAX_TITLE_LENGTH = 45
            private const val MAX_URL_LENGTH = 40
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        /*
        val newView: View
        val viewHolder: TabListItemViewHolder

        val tabSession = this.getItem(position) as TabSessionState
        val selectedState = (context.components.core.store.state.selectedTabId == tabSession.id)

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.tablist_item, parent, false)
            viewHolder = TabListItemViewHolder(newView, context, selectedCallback, deletedCallback)
            viewHolder.setup(tabSession, selectedState)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as TabListItemViewHolder
            viewHolder.setup(tabSession, selectedState)
            newView = convertView
        }
        */

        // if (position < tabs.count()) {
        //   viewHolder.setup(tabSession, selectedState)
        // }

        // viewHolder.setThumbnail(tabSession.toTab())
        // viewHolder.setSelected(selectedState)

        val tab = this.getItem(position) as Tab
        val selectedState = (context.components.core.store.state.selectedTabId == tab.id)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView = inflater.inflate(R.layout.tablist_item, parent, false)
        val viewHolder = TabListItemViewHolder(newView, context, selectedCallback, {
            if (it != null) {
                context.components.useCases.tabsUseCases.removeTab.invoke(it.id)
                // this.tabChanged()
            }
        })
        viewHolder.setup(tab, selectedState)
        newView.tag = viewHolder

        return newView
    }

    override fun getItem(position: Int): Any {
        return tabs!!.list[tabs!!.list.count() - 1 - position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return tabs?.list?.count() ?: 0
    }

    fun tabChanged(tabs: Tabs) {
        this.tabs = tabs
        this.notifyDataSetChanged()
    }
}