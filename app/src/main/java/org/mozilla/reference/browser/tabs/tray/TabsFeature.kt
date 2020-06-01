package org.mozilla.reference.browser.tabs.tray


import androidx.annotation.VisibleForTesting
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.ThumbnailsUseCases
import mozilla.components.concept.tabstray.Tab
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.tabs.tabstray.TabsTrayInteractor
import mozilla.components.support.base.feature.LifecycleAwareFeature

/**
 * Feature implementation for connecting a tabs tray implementation with the session module.
 *
 * @param defaultTabsFilter A tab filter that is used for the initial presenting of tabs that will be used by
 * [TabsFeature.filterTabs] by default as well.
 */
class TabsFeature(
        tabsTray: TabsTray,
        private val store: BrowserStore,
        tabsUseCases: TabsUseCases,
        thumbnailsUseCases: ThumbnailsUseCases? = null,
        private val defaultTabsFilter: (TabSessionState) -> Boolean = { true },
        closeTabsTray: () -> Unit
) : LifecycleAwareFeature {
    @VisibleForTesting
    internal var presenter = TabsTrayPresenter(
            tabsTray,
            store,
            thumbnailsUseCases,
            defaultTabsFilter,
            closeTabsTray
    )

    @VisibleForTesting
    internal var interactor = TabsTrayInteractor(
            tabsTray,
            tabsUseCases.selectTab,
            tabsUseCases.removeTab,
            closeTabsTray)

    override fun start() {
        presenter.start()
        interactor.start()
    }

    override fun stop() {
        presenter.stop()
        interactor.stop()
    }

    /**
     * Filter the list of tabs using [tabsFilter].
     *
     * @param tabsFilter A filter function returning `true` for all tabs that should be displayed in
     * the tabs tray. Uses the [defaultTabsFilter] if none is provided.
     */
    fun filterTabs(tabsFilter: (TabSessionState) -> Boolean = defaultTabsFilter) {
        presenter.tabsFilter = tabsFilter

        val state = store.state
        presenter.updateTabs(state.toTabs(tabsFilter))
    }
}

internal fun TabSessionState.toTab() = Tab(
        id,
        content.url,
        content.title,
        content.icon,
        content.thumbnail
)

internal fun BrowserState.toTabs(
        tabsFilter: (TabSessionState) -> Boolean = { true }
): Tabs {
    val list = tabs.filter(tabsFilter).map { it.toTab() }
    return Tabs(list, list.indexOfFirst { it.id == selectedTabId })
}