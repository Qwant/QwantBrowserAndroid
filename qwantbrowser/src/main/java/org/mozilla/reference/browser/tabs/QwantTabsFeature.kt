package org.mozilla.reference.browser.tabs

import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.feature.tabs.ext.toTabs
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged

class QwantTabsFeature(
        private val store: BrowserStore,
        private val tabsChanged: (Tabs) -> Unit,
        internal var tabsFilter: (TabSessionState) -> Boolean,
) : LifecycleAwareFeature {
    private var tabs: Tabs? = null
    private var scope: CoroutineScope? = null

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { it.toTabs(tabsFilter) }
            .ifChanged()
            .collect { tabs ->
                // Do not invoke the callback on start if this is the initial state.
                updateTabs(tabs)
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }

    internal fun updateTabs(tabs: Tabs) {
        this.tabs = tabs
        tabsChanged.invoke(tabs)
        /* val currentTabs = this.tabs

        if (currentTabs == null) {
            this.tabs = tabs
            if (tabs.list.isNotEmpty()) {
                // tabsTray.updateTabs(tabs)
                // tabsTray.onTabsInserted(0, tabs.list.size)
            }
            return
        } else {
            calculateDiffAndUpdateTabsTray(currentTabs, tabs)
        } */
    }

    private fun calculateDiffAndUpdateTabsTray(currentTabs: Tabs, updatedTabs: Tabs) {
        val result = DiffUtil.calculateDiff(DiffCallback(currentTabs, updatedTabs), false)

        this.tabs = updatedTabs

        // tabsTray.updateTabs(updatedTabs)

        /* result.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                tabsTray.onTabsChanged(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                tabsTray.onTabsMoved(fromPosition, toPosition)
            }

            override fun onInserted(position: Int, count: Int) {
                tabsTray.onTabsInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                tabsTray.onTabsRemoved(position, count)
            }
        }) */
    }
}

internal class DiffCallback(
        private val currentTabs: Tabs,
        private val updatedTabs: Tabs
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            currentTabs.list[oldItemPosition].id == updatedTabs.list[newItemPosition].id

    override fun getOldListSize(): Int = currentTabs.list.size

    override fun getNewListSize(): Int = updatedTabs.list.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldItemPosition == currentTabs.selectedIndex && newItemPosition != updatedTabs.selectedIndex) {
            // This item was previously selected and is not selected anymore (-> changed).
            return false
        }

        if (newItemPosition == updatedTabs.selectedIndex && oldItemPosition != currentTabs.selectedIndex) {
            // This item was not selected previously and is now selected (-> changed).
            return false
        }

        return updatedTabs.list[newItemPosition] == currentTabs.list[oldItemPosition]
    }
}