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
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.storage.BookmarkItemV2

class BookmarksAdapter(
        private val context: Context,
        private val bookmarkStorage: BookmarksStorage,
        private val bookmarkSelectedCallback: (bookmarkItem: BookmarkItemV2, private: Boolean) -> Unit,
        private val bookmarkEditCallback: (bookmarkItem: BookmarkItemV2) -> Unit,
        parent: BookmarkItemV2? = null
) : BaseAdapter() {
    private var list: ArrayList<BookmarkItemV2>? = if (parent == null) bookmarkStorage.root() else parent.children

    fun setParent(parent: BookmarkItemV2?) {
        list = if (parent == null) bookmarkStorage.root() else parent.children
        this.notifyDataSetChanged()
    }

    internal class BookmarkItemViewHolder(
            item_layout: View,
            private val bookmarksAdapter: BookmarksAdapter,
            private val bookmarkStorage: BookmarksStorage,
            private val selectedCallback: (bookmarkItem: BookmarkItemV2, private: Boolean) -> Unit,
            private val bookmarkEditCallback: (bookmarkItem: BookmarkItemV2) -> Unit,
            private val context: Context
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemLayout: LinearLayout = item_layout.findViewById(R.id.bookmark_item_layout)
        private var itemIcon: ImageView = item_layout.findViewById(R.id.bookmark_item_icon)
        private var itemIconBackground: ImageView = item_layout.findViewById(R.id.bookmark_item_icon_background)
        private var itemTitle: TextView = item_layout.findViewById(R.id.bookmark_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.bookmark_url)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.bookmark_item_layout_text)
        private var itemButtonMenu: MenuButton = item_layout.findViewById(R.id.bookmark_item_menu_button)

        fun setupBookmark(bookmarkItem: BookmarkItemV2) {
            this.itemUrl.visibility = View.VISIBLE

            if (bookmarkItem.icon != null) this.itemIcon.setImageBitmap(bookmarkItem.icon!!.bitmap)
            else {
                // val icon = context.components.core.icons.loadIcon()
                context.components.core.icons.loadIntoView(
                    itemIcon,
                    IconRequest(bookmarkItem.url ?: ""),
                    ContextCompat.getDrawable(context, R.drawable.ic_bookmark_default_fav)
                ).start()
            }

            itemIconBackground.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bookmark_icon_background))

            val title = if (bookmarkItem.title.length > MAX_TITLE_LENGTH) bookmarkItem.title.substring(0, MAX_TITLE_LENGTH - 3) + "..." else bookmarkItem.title

            var url: String? = bookmarkItem.url
            if (url != null) {
                if (url.startsWith("http://www.")) url = url.substring(11)
                else if (url.startsWith("https://www.")) url = url.substring(12)
                url = if (url.length > MAX_URL_LENGTH) url.substring(0, MAX_URL_LENGTH - 3) + "..." else url
            } else  url = ""

            this.itemTitle.text = title
            this.itemUrl.text = url
            this.itemLayoutText.setOnClickListener {
                selectedCallback.invoke(bookmarkItem, false)
            }

            itemButtonMenu.setColorFilter(ContextCompat.getColor(context, context.theme.resolveAttribute(R.attr.qwant_color_main)))
            itemButtonMenu.menuBuilder = BrowserMenuBuilder(listOf(
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_open_link_in_new_tab),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_newtab
                ) {
                    selectedCallback.invoke(bookmarkItem, false)
                },
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_open_link_in_private_tab),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_newtab_private
                ) {
                    // context.components.useCases.tabsUseCases.addPrivateTab.invoke(bookmarkItem.url!!, selectTab = true)
                    selectedCallback.invoke(bookmarkItem, true)
                },
                BrowserMenuImageText(
                        context.getString(R.string.mozac_feature_contextmenu_copy_link),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_clipboard
                ) {
                    val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("Copied URL", bookmarkItem.url)
                    clipboard?.setPrimaryClip(clip)
                },

                BrowserMenuImageText(
                        context.getString(R.string.bookmarks_modify),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_edit
                ) {
                    bookmarkEditCallback.invoke(bookmarkItem)
                },

                BrowserMenuImageText(
                        context.getString(R.string.bookmarks_delete),
                        textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                        imageResource = R.drawable.ic_ctmenu_delete
                ) {
                    bookmarkStorage.deleteBookmark(bookmarkItem)
                    bookmarksAdapter.notifyDataSetChanged()
                }
            ))
        }

        fun setupFolder(bookmarkItem: BookmarkItemV2) {
            this.itemUrl.visibility = View.GONE

            this.itemIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_folder))
            this.itemIconBackground.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_icon_background))

            this.itemTitle.text = bookmarkItem.title

            this.itemLayout.setOnClickListener {
                selectedCallback.invoke(bookmarkItem, false)
            }

            itemButtonMenu.setColorFilter(ContextCompat.getColor(context, context.theme.resolveAttribute(R.attr.qwant_color_main)))
            itemButtonMenu.menuBuilder = BrowserMenuBuilder(listOf(
                    BrowserMenuImageText(
                            context.getString(R.string.bookmarks_modify_folder),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_ctmenu_edit
                    ) {
                        bookmarkEditCallback.invoke(bookmarkItem)
                    },
                    BrowserMenuImageText(
                            context.getString(R.string.bookmarks_delete),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_ctmenu_delete
                    ) {
                        bookmarkStorage.deleteBookmark(bookmarkItem)
                        bookmarksAdapter.notifyDataSetChanged()
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
            viewHolder = BookmarkItemViewHolder(newView, this, this.bookmarkStorage, this.bookmarkSelectedCallback, this.bookmarkEditCallback, this.context)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as BookmarkItemViewHolder
            newView = convertView
        }

        if (this.list != null && position < this.list!!.count()) {
            val bookmark = this.list!![position]
            if (bookmark.type == BookmarkItemV2.BookmarkType.BOOKMARK) {
                viewHolder.setupBookmark(bookmark)
            } else viewHolder.setupFolder(bookmark)
        }

        return newView
    }

    override fun getItem(position: Int): Any {
        return this.list!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        if (this.list != null)
            return this.list!!.count()
        return 0
    }
}