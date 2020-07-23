package org.mozilla.reference.browser.storage.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.storage.BookmarkItemV1

class BookmarksAdapter(
        private val context: Context,
        private val bookmarksStorage: BookmarksStorage,
        private val bookmarkSelectedCallback: (bookmarkItem: BookmarkItemV1) -> Unit
) : BaseAdapter() {
    internal class BookmarkItemViewHolder(
            item_layout: View,
            private val bookmarkStorage: BookmarksStorage,
            private val selectedCallback: (bookmarkItem: BookmarkItemV1) -> Unit,
            private val context: Context
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemIcon: ImageView = item_layout.findViewById(R.id.bookmark_item_icon)
        private var itemTitle: TextView = item_layout.findViewById(R.id.bookmark_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.bookmark_url)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.bookmark_item_layout_text)
        private var itemButtonMenu: MenuButton = item_layout.findViewById(R.id.bookmark_item_menu_button)

        fun setup(bookmarkItem: BookmarkItemV1) {
            if (bookmarkItem.icon != null) this.itemIcon.setImageBitmap(bookmarkItem.icon.bitmap)

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
                    val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("Copied URL", bookmarkItem.url)
                    clipboard?.primaryClip = clip
                },
                BrowserMenuImageText(
                        context.getString(R.string.bookmarks_delete),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_trash
                ) {
                    bookmarkStorage.deleteBookmark(bookmarkItem)
                }
            ))
        }

        companion object {
            private const val MAX_TITLE_LENGTH = 35
            private const val MAX_URL_LENGTH = 40
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View
        val viewHolder: BookmarkItemViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.bookmarks_list_item, parent, false)
            viewHolder = BookmarkItemViewHolder(newView, this.bookmarksStorage, this.bookmarkSelectedCallback, this.context)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as BookmarkItemViewHolder
            newView = convertView
        }

        if (position < this.bookmarksStorage.count()) {
            viewHolder.setup(this.bookmarksStorage.get(position))
        }

        return newView
    }

    override fun getItem(position: Int): Any {
        return this.bookmarksStorage.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return this.bookmarksStorage.count()
    }
}