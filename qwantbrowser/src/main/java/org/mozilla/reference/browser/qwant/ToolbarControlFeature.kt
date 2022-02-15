package org.mozilla.reference.browser.qwant

import android.content.Context
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.reference.browser.R

class ToolbarControlFeature(
        val context: Context,
        val store: BrowserStore,
        val toolbar: Toolbar
): LifecycleAwareFeature {
    private var scope: CoroutineScope? = null

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.selectedTab?.content?.url }
            .ifChanged()
            .collect { url ->
                if (url.startsWith(context.getString(R.string.homepage_startwith_filter))) {
                    Log.d("QWANT_BROWSER_URLBAR", "should hide urlbar")
                    toolbar.asView().visibility = View.GONE
                } else {
                    Log.d("QWANT_BROWSER_URLBAR", "should show urlbar")
                    toolbar.asView().visibility = View.VISIBLE
                }
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}