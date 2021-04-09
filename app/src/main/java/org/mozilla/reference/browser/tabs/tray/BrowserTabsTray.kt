package org.mozilla.reference.browser.tabs.tray

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R

/* const val DEFAULT_ITEM_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
const val DEFAULT_ITEM_BACKGROUND_SELECTED_COLOR = 0xFFFF45A1FF.toInt()
const val DEFAULT_ITEM_TEXT_COLOR = 0xFF111111.toInt()
const val DEFAULT_ITEM_TEXT_SELECTED_COLOR = 0xFFFFFFFF.toInt() */

/**
 * A customizable tabs tray for browsers.
 */
class BrowserTabsTray @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        private val tabsAdapter: TabsAdapter = TabsAdapter(context)
) : RecyclerView(context, attrs, defStyleAttr),
        TabsTray by tabsAdapter {

        internal val styling: TabsTrayStyling
        private var onSessionsChangedCallback: (() -> Unit)? = null

        init {
                tabsAdapter.tabsTray = this

                this.setPadding(0, 10.dpToPx(Resources.getSystem().displayMetrics), 0, 0)

                layoutManager = GridLayoutManager(context, 2)
                adapter = tabsAdapter

                // val attr = context.obtainStyledAttributes(attrs, R.attr, defStyleAttr, 0)
                styling = TabsTrayStyling(
                        R.drawable.background_tabstray_item,
                        R.drawable.background_tabstray_item_selected,
                        context.theme.resolveAttribute(R.attr.tabsTrayItemBackgroundColor),
                        context.theme.resolveAttribute(R.attr.tabsTraySelectedItemBackgroundColor),
                        R.color.close_tab_button,
                        context.theme.resolveAttribute(R.attr.tabsTraySelectedItemTextColor),
                        /* R.attr.getColor(R.styleable.BrowserTabsTray_tabsTrayItemBackgroundColor, DEFAULT_ITEM_BACKGROUND_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTraySelectedItemBackgroundColor, DEFAULT_ITEM_BACKGROUND_SELECTED_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTrayItemTextColor, DEFAULT_ITEM_TEXT_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTraySelectedItemTextColor, DEFAULT_ITEM_TEXT_SELECTED_COLOR), */
                        4.dpToPx(resources.displayMetrics).toFloat()  // attr.getDimensionPixelSize(context.getat, 0).toFloat()
                )
                // attr.recycle()
        }

        override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()

                // Multiple tabs that we are currently displaying may be subscribed to a Session to update
                // automatically. Unsubscribe all of them now so that we do not reference them and keep them
                // in memory.
                tabsAdapter.unsubscribeHolders()
        }

        /**
         * Convenience method to cast this object to an Android View object.
         */
        override fun asView(): View {
                return this
        }

        override fun onTabsChanged(position: Int, count: Int) {
                this.onSessionsChangedCallback?.invoke()
        }

        fun onTabsChangedRegister(callback: () -> Unit) {
                this.onSessionsChangedCallback = callback
        }
}

internal data class TabsTrayStyling(
        val itemBackground: Int,
        val itemBackgroundSelected: Int,
        val itemBackgroundColor: Int,
        val selectedItemBackgroundColor: Int,
        val itemTextColor: Int,
        val selectedItemTextColor: Int,
        val itemElevation: Float
)
