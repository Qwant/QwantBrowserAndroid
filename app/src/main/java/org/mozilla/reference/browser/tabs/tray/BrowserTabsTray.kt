package org.mozilla.reference.browser.tabs.tray

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.tabstray.TabsTray
import org.mozilla.reference.browser.R

const val DEFAULT_ITEM_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
const val DEFAULT_ITEM_BACKGROUND_SELECTED_COLOR = 0xFFFF45A1FF.toInt()
const val DEFAULT_ITEM_TEXT_COLOR = 0xFF111111.toInt()
const val DEFAULT_ITEM_TEXT_SELECTED_COLOR = 0xFFFFFFFF.toInt()

/**
 * A customizable tabs tray for browsers.
 */
class BrowserTabsTray @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        val tabsAdapter: TabsAdapter = TabsAdapter()
) : RecyclerView(context, attrs, defStyleAttr),
        TabsTray by tabsAdapter {

        internal val styling: TabsTrayStyling

        init {
                tabsAdapter.tabsTray = this

                layoutManager = GridLayoutManager(context, 2)
                adapter = tabsAdapter

                val attr = context.obtainStyledAttributes(attrs, R.styleable.BrowserTabsTray, defStyleAttr, 0)
                styling = TabsTrayStyling(
                        R.drawable.background_tabstray_item,
                        R.drawable.background_tabstray_item_selected,
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTrayItemBackgroundColor, DEFAULT_ITEM_BACKGROUND_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTraySelectedItemBackgroundColor, DEFAULT_ITEM_BACKGROUND_SELECTED_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTrayItemTextColor, DEFAULT_ITEM_TEXT_COLOR),
                        attr.getColor(R.styleable.BrowserTabsTray_tabsTraySelectedItemTextColor, DEFAULT_ITEM_TEXT_SELECTED_COLOR),
                        attr.getDimensionPixelSize(R.styleable.BrowserTabsTray_tabsTrayItemElevation, 0).toFloat()
                )
                attr.recycle()
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
