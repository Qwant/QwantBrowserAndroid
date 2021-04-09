package org.mozilla.reference.browser.layout

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged

class QwantBarFeature(
        val store: BrowserStore,
        val qwantBar: QwantBar
): LifecycleAwareFeature {
    private var urlChangedScope: CoroutineScope? = null
    private var tabCountChangedScope: CoroutineScope? = null
    private var privacyChangedScope: CoroutineScope? = null
    private var canForwardChangedScope: CoroutineScope? = null
    // private var canBackwardChangedScope: CoroutineScope? = null
    // private var fullscreenChangedScope: CoroutineScope? = null

    override fun start() {
        urlChangedScope = store.flowScoped { flow -> flow
            .ifAnyChanged { arrayOf(it.selectedTabId, it.selectedTab?.content?.url)}
            .collect { state ->
                Log.d("QWANT_BROWSER", "flow scope observer: url changed or tab switched")
                qwantBar.checkSession(state.selectedTab?.content?.url)
            }
        }
        tabCountChangedScope = store.flowScoped { flow -> flow
            .ifChanged { state -> state.tabs.size }
            .collect {
                Log.d("QWANT_BROWSER", "flow scope observer: tab count changed")
                qwantBar.updateTabCount()
            }
        }
        privacyChangedScope = store.flowScoped { flow -> flow
                .mapNotNull { state -> state.selectedTab?.content?.private }
                .ifChanged()
                .collect {
                    Log.d("QWANT_BROWSER", "flow scope observer: privacy changed")
                    qwantBar.setPrivacyMode(it)
                }
        }
        canForwardChangedScope = store.flowScoped { flow -> flow
            .mapNotNull { state -> state.selectedTab?.content?.canGoForward }
            .ifChanged()
            .collect {
                Log.d("QWANT_BROWSER", "flow scope observer: can forward changed")
                qwantBar.changeForwardButton(it)
            }
        }
        /* canBackwardChangedScope = store.flowScoped { flow -> flow
            .mapNotNull { state -> state.selectedTab?.content?.canGoBack }
            .ifChanged()
            .collect {
                Log.d("QWANT_BROWSER", "flow scope observer: can backward changed")
                qwantBar.changeBackwardButton(it)
            }
        } */
    }

    override fun stop() {
        urlChangedScope?.cancel()
        tabCountChangedScope?.cancel()
        canForwardChangedScope?.cancel()
        privacyChangedScope?.cancel()
    }
}