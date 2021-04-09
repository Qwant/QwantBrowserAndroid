package org.mozilla.reference.browser.tabs.tray

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import org.mozilla.reference.browser.R

/**
 * RecyclerView adapter implementation to display a list/grid of tabs.
 */
@Suppress("TooManyFunctions")
class TabsAdapter(
        val context: Context,
        delegate: Observable<TabsTray.Observer> = ObserverRegistry()
) : RecyclerView.Adapter<TabViewHolder>(),
        TabsTray,
        Observable<TabsTray.Observer> by delegate {

    internal lateinit var tabsTray: BrowserTabsTray

    private var tabs: Tabs = Tabs(listOf(), 0)
    private val holders = mutableListOf<TabViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tabstray_item,
                parent,
                false),
                tabsTray
        ).also {
            holders.add(it)
        }
    }

    override fun getItemCount() = tabs.list.size

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs.list[position], position == tabs.selectedIndex, this)
    }

    override fun onViewRecycled(holder: TabViewHolder) {
        holder.unbind()
    }

    fun unsubscribeHolders() {
        holders.forEach { it.unbind() }
        holders.clear()
    }

    override fun updateTabs(tabs: Tabs) {
        this.tabs = tabs
        notifyDataSetChanged()
    }

    override fun onTabsInserted(position: Int, count: Int) =
        notifyItemRangeInserted(position, count)
    override fun onTabsRemoved(position: Int, count: Int) =
        notifyItemRangeRemoved(position, count)
    override fun onTabsMoved(fromPosition: Int, toPosition: Int) =
        notifyItemMoved(fromPosition, toPosition)

    override fun isTabSelected(tabs: Tabs, position: Int): Boolean {
        return tabs.selectedIndex == position
    }

    override fun onTabsChanged(position: Int, count: Int) =
        notifyItemChanged(position, null)
}
