package org.mozilla.reference.browser.tabs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.reference.browser.ext.components

class QwantTabsFeature(
        private val store: BrowserStore,
        private val tabsChanged: () -> Unit,
        internal var tabsFilter: (TabSessionState) -> Boolean,
) : LifecycleAwareFeature {
    private var scope: CoroutineScope? = null

    override fun start() {
        scope = store.flowScoped { flow -> flow
            .ifChanged { state -> state.tabs.size }
            .collect {
                tabsChanged.invoke()
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}