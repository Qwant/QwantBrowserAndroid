package org.mozilla.reference.browser.storage.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.R

class HistoryAdapter(
        private val context: Context,
        private val visits: List<VisitInfo>,
        private val historyItemSelectedCallback: (historyItem: VisitInfo) -> Unit
) : BaseAdapter() {
    internal class HistoryItemViewHolder(
            item_layout: View,
            private val historyItemSelectedCallback: (historyItem: VisitInfo) -> Unit,
            private val context: Context
    ) : RecyclerView.ViewHolder(item_layout) {
        private val MAX_TITLE_LENGTH = 35
        private val MAX_URL_LENGTH = 40

        /* private var itemIcon: ImageView = item_layout.findViewById(R.id.bookmark_item_icon)
        private var itemTitle: TextView = item_layout.findViewById(R.id.bookmark_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.bookmark_url)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.bookmark_item_layout_text)
        private var itemButtonMenu: MenuButton = item_layout.findViewById(R.id.bookmark_item_menu_button) */

        fun setup(visit: VisitInfo) {
            /* if (bookmarkItem.icon != null) this.itemIcon.setImageBitmap(bookmarkItem.icon.bitmap)

            val title = if (bookmarkItem.title.length > MAX_TITLE_LENGTH) bookmarkItem.title.substring(0, MAX_TITLE_LENGTH - 3) + "..." else bookmarkItem.title
            val url = if (bookmarkItem.url.length > MAX_URL_LENGTH) bookmarkItem.url.substring(0, MAX_URL_LENGTH - 3) + "..." else bookmarkItem.url

            this.itemTitle.text = title
            this.itemUrl.text = url
            this.itemLayoutText.setOnClickListener {
                context.components.useCases.sessionUseCases.loadUrl(bookmarkItem.url)
                selectedCallback.invoke(bookmarkItem)
            }

            itemButtonMenu.setColorFilter(ContextCompat.getColor(context, context.theme.resolveAttribute(R.attr.qwant_color_main)))
            itemButtonMenu.menuBuilder = BrowserMenuBuilder(listOf(
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_open_link_in_new_tab),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_newtab
                ) {
                    context.components.useCases.tabsUseCases.addTab.invoke(bookmarkItem.url, selectTab = true)
                    selectedCallback.invoke(bookmarkItem)
                },
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_open_link_in_private_tab),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_newtab_private
                ) {
                    context.components.useCases.tabsUseCases.addPrivateTab.invoke(bookmarkItem.url, selectTab = true)
                    selectedCallback.invoke(bookmarkItem)
                },
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_copy_link),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_clipboard
                ) {
                    // TODO Copy url to clipboard
                },
                BrowserMenuImageText(
                        context.getString(R.string.bookmarks_delete),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_trash
                ) {
                    bookmarkStorage.deleteBookmark(bookmarkItem)
                }
            )) */
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View
        val viewHolder: HistoryItemViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.history_list_item, parent, false)
            viewHolder = HistoryItemViewHolder(newView, this.historyItemSelectedCallback, this.context)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as HistoryItemViewHolder
            newView = convertView
        }

        if (position < this.visits.size) {
            viewHolder.setup(this.visits[position])
        }

        return newView
    }

    override fun getItem(position: Int): Any {
        return this.visits[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return this.visits.size
    }
}