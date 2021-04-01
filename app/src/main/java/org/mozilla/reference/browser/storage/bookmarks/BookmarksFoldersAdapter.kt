package org.mozilla.reference.browser.storage.bookmarks

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.storage.BookmarkItemV2

class BookmarksFoldersAdapter(
        val context: Context,
        private val forBookmark: BookmarkItemV2?,
        private val bookmarksRoot: ArrayList<BookmarkItemV2>
) : BaseAdapter()  {
    internal class FolderData(
            val bookmarkItem: BookmarkItemV2?,

            val level: Int = 0,
            var opened: Boolean = false,

            val parent: FolderData? = null,
            val children: ArrayList<FolderData> = arrayListOf()
    )

    internal class FolderDataViewHolder(
            item_layout: View,
            private val bookmarksFoldersAdapter: BookmarksFoldersAdapter
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemLayout: LinearLayout = item_layout.findViewById(R.id.folder_item_layout)
        private var itemArrow: ImageView = item_layout.findViewById(R.id.folder_item_arrow)
        private var itemTitle: TextView = item_layout.findViewById(R.id.folder_item_title)

        private val scale: Float = itemLayout.context.resources.displayMetrics.density

        private fun getPaddingOffset(level: Int) : Int {
            return (DP_BY_LEVEL * level * scale + 0.5f).toInt()
        }

        fun setup(folderData: FolderData) {
            itemTitle.text = folderData.bookmarkItem?.title ?: itemLayout.context.getString(R.string.bookmarks)

            // set appropriate arrow
            if (folderData.bookmarkItem != null && (folderData.bookmarkItem.children?.filter { it.type == BookmarkItemV2.BookmarkType.FOLDER }?.count() ?: 0) == 0) {
                itemArrow.visibility = View.INVISIBLE
            } else {
                itemArrow.visibility = View.VISIBLE
                itemArrow.rotation = if (folderData.opened) 90.0f else 0.0f
            }

            // selected or not background and text color
            if (folderData === bookmarksFoldersAdapter.selectedItem) {
                itemTitle.setTextColor(ContextCompat.getColor(itemLayout.context, R.color.qwant_selected_text))
                itemTitle.setBackgroundColor(ContextCompat.getColor(itemLayout.context, R.color.qwant_new_selected_color))
            } else {
                itemTitle.setTextColor(ContextCompat.getColor(itemLayout.context, R.color.menu_items))
                itemTitle.setBackgroundColor(Color.TRANSPARENT)
            }

            // offset by level
            itemLayout.setPadding(this.getPaddingOffset(folderData.level), itemLayout.paddingTop, itemLayout.paddingRight, itemLayout.paddingBottom)

            // on click title: select and render
            itemTitle.setOnClickListener {
                if (folderData !== bookmarksFoldersAdapter.selectedItem) {
                    bookmarksFoldersAdapter.selectedItem = folderData
                    bookmarksFoldersAdapter.renderDataTreeToList()
                }
            }
            // on click arrow: open/close folder and render
            itemArrow.setOnClickListener {
                folderData.opened = !folderData.opened
                bookmarksFoldersAdapter.renderDataTreeToList()
            }
        }

        companion object {
            const val DP_BY_LEVEL = 25
        }
    }

    private val folderDataTreeRoot = FolderData(null, 0, (forBookmark?.parent != null))
    private val folderDataList = arrayListOf<FolderData>()

    private var selectedItem: FolderData = folderDataTreeRoot

    init {
        reset()
    }

    fun reset() {
        selectedItem = folderDataTreeRoot
        folderDataTreeRoot.children.clear()
        loadDataTree(folderDataTreeRoot)
        renderDataTreeToList()
    }

    fun getSelectedBookmark() : BookmarkItemV2? {
        return selectedItem.bookmarkItem
    }

    private fun loadDataTree(parentData: FolderData, level: Int = 1) {
        val l = if (parentData.bookmarkItem != null) parentData.bookmarkItem.children else bookmarksRoot
        l?.filter { it.type == BookmarkItemV2.BookmarkType.FOLDER }?.forEach {
            if (forBookmark !== it) {
                val opened = (forBookmark != null && it.isParentOf(forBookmark))
                val data = FolderData(it, level, opened = opened, parent = parentData)

                if (it === forBookmark?.parent) selectedItem = data

                loadDataTree(data, level + 1)

                parentData.children.add(data)
            }
        }
    }

    private fun renderDataTreeToList(folder: FolderData? = null) {
        folderDataList.clear()
        renderDataTreeToListRec(folder ?: folderDataTreeRoot)
        this.notifyDataSetChanged()
    }

    private fun renderDataTreeToListRec(folder: FolderData) {
        folderDataList.add(folder)
        if (folder.opened) {
            folder.children.forEach { renderDataTreeToListRec(it) }
        }
    }

    override fun getCount(): Int {
        return folderDataList.count()
    }

    override fun getItem(position: Int): Any {
        return folderDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View

        val viewHolder: FolderDataViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.bookmarks_folder_list_item, parent, false)
            viewHolder = FolderDataViewHolder(newView, this)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as FolderDataViewHolder
            newView = convertView
        }

        if (position < this.folderDataList.count()) {
            viewHolder.setup(this.folderDataList[position])
        }

        return newView
    }
}