package org.mozilla.reference.browser.storage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.assist.HistoryAdapter
import java.io.*
import java.util.*

class BookmarksAdapter(
        private val context: Context,
        private val bookmarksStorage: BookmarksStorage
) : BaseAdapter() {

    internal class BookmarkItemViewHolder(item_layout: View) : RecyclerView.ViewHolder(item_layout) {
        private var item_title: TextView = item_layout.findViewById(R.id.bookmark_title)
        private var item_url: TextView = item_layout.findViewById(R.id.bookmark_url)
        private var item_favicon: TextView = item_layout.findViewById(R.id.bookmark_title)

        fun setup(bookmarkItem: BookmarkItem) {
            this.item_title.text = bookmarkItem.title
            this.item_url.text = bookmarkItem.url
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View
        val viewHolder: BookmarkItemViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.bookmarks_list_item, parent, false)
            viewHolder = BookmarkItemViewHolder(newView)
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