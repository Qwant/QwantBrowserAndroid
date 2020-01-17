package org.mozilla.reference.browser.storage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class BookmarksAdapter(
        private val context: Context,
        private val bookmarksStorage: BookmarksStorage,
        private val bookmarkSelectedCallback: (bookmarkItem: BookmarkItem) -> Unit
) : BaseAdapter() {

    internal class BookmarkItemViewHolder(
            item_layout: View,
            private val bookmarkStorage: BookmarksStorage,
            private val selectedCallback: (bookmarkItem: BookmarkItem) -> Unit,
            private val context: Context
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemTitle: TextView = item_layout.findViewById(R.id.bookmark_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.bookmark_url)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.bookmark_item_layout_text)
        private var itemButtonDelete: Button = item_layout.findViewById(R.id.bookmark_item_delete)

        fun setup(bookmarkItem: BookmarkItem) {
            this.itemTitle.text = bookmarkItem.title
            this.itemUrl.text = bookmarkItem.url
            this.itemLayoutText.setOnClickListener {
                context.components.useCases.sessionUseCases.loadUrl(bookmarkItem.url)
                selectedCallback.invoke(bookmarkItem)
            }
            this.itemButtonDelete.setOnClickListener {
                bookmarkStorage.deleteBookmark(bookmarkItem)
            }
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