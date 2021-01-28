package org.mozilla.reference.browser.tabs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.concept.tabstray.Tab
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class TabsRecyclerAdapter(
        private val context: Context,
        private val thumbnailLoader: ThumbnailLoader,
        private var isPrivate: Boolean,
        private val selectedCallback: (tab: Tab?) -> Unit,
        private val deletedCallback: (tabSession: TabSessionState?) -> Unit
)  : RecyclerView.Adapter<TabListItemRecycleViewHolder>() {
    var tabs = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabListItemRecycleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val newView = inflater.inflate(R.layout.tablist_item, parent, false)
        return TabListItemRecycleViewHolder(newView, context, thumbnailLoader, selectedCallback, deletedCallback)
    }

    override fun onBindViewHolder(holder: TabListItemRecycleViewHolder, position: Int) {
        val tabSession = this.getItem(position)
        val selectedState = (context.components.core.store.state.selectedTabId == tabSession.id)
        holder.bind(tabSession, selectedState)
    }

    override fun getItemCount(): Int {
        return tabs.count()
    }

    private fun getItem(position: Int): TabSessionState {
        return tabs[tabs.count() - 1 - position]
    }

    fun tabChanged() {
        tabs = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate)
        this.notifyDataSetChanged()
    }

    /* override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val tabSession = this.getItem(position) as TabSessionState
        val selectedState = (context.components.core.store.state.selectedTabId == tabSession.id)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView = inflater.inflate(R.layout.tablist_item, parent, false)
        val viewHolder = TabListItemViewHolder(newView, context, selectedCallback, deletedCallback)
        viewHolder.setup(tabSession, selectedState)
        newView.tag = viewHolder

        return newView
    } */

    /* override fun getItem(position: Int): Any {
        return tabs[tabs.count() - 1 - position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return tabs.count()
    }

    fun tabChanged() {
        tabs = context.components.core.store.state.getNormalOrPrivateTabs(isPrivate)
        this.notifyDataSetChanged()
    } */
}