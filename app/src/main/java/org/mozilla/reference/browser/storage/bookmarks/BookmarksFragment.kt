/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.storage.bookmarks

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.storage.BookmarkItemV2


/**
 * A fragment for displaying the tabs tray.
 */
class BookmarksFragment: Fragment(), UserInteractionHandler {
    private var bookmarksStorage: BookmarksStorage? = null
    fun setBookmarkStorage(bookmarksStorage: BookmarksStorage) { this.bookmarksStorage = bookmarksStorage }
    private var bookmarksClosedCallback: (() -> Unit)? = null
    fun setBookmarksClosedCallback(cb: () -> Unit) { this.bookmarksClosedCallback = cb }

    var toolbar: Toolbar? = null

    private var adapter: BookmarksAdapter? = null

    private var listview: ListView? = null
    private var layoutNoResult: LinearLayout? = null

    private var folder: BookmarkItemV2? = null

    private var editAfterCreation: BookmarkItemV2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("EditBookmarkResult") { _, bundle ->
            toolbar?.title = folder?.title ?: context?.getString(R.string.bookmarks)
            val result = bundle.getBoolean("bookmark_updated")
            if (result) {
                bookmarksStorage?.persist()
                editAfterCreation = null
            } else if (editAfterCreation != null) {
                bookmarksStorage?.deleteBookmark(editAfterCreation!!)
                editAfterCreation = null
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        if (savedInstanceState != null) {
            folder = savedInstanceState.getParcelable("folder")
        }

        toolbar = view.findViewById(R.id.bookmarks_toolbar)

        listview = view.findViewById(R.id.bookmarks_listview)
        layoutNoResult = view.findViewById(R.id.bookmarks_noresult_layout)

        if (bookmarksStorage != null) adapter = BookmarksAdapter(requireContext(), bookmarksStorage!!, ::bookmarkSelected, ::bookmarkEdit, folder)
        bookmarksStorage?.onChange(::storageChanged)
        listview!!.adapter = adapter
        listview!!.dividerHeight = 0

        if (bookmarksStorage?.count() == 0) {
            listview!!.visibility = View.GONE
            layoutNoResult!!.visibility = View.VISIBLE
        }

        val addFolderButton: ImageButton = view.findViewById(R.id.bookmarks_add_folder)
        addFolderButton.setOnClickListener {
            val newFolder = BookmarkItemV2(BookmarkItemV2.BookmarkType.FOLDER, "", parent = folder)
            bookmarksStorage?.addBookmark(newFolder)
            bookmarkEdit(newFolder)
            editAfterCreation = newFolder
        }

        toolbar?.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.qwant_text))
        toolbar?.setNavigationOnClickListener { this.onBackPressed() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.title = folder?.title ?: context?.getString(R.string.bookmarks)
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.putParcelable("folder", folder)
    }

    override fun onBackPressed(): Boolean {
        if (folder?.title == null) {
            this.closeBookmarks()
        } else {
            if (folder?.parent != null) {
                toolbar?.title = folder?.parent?.title ?: context?.getString(R.string.bookmarks)
                listview?.adapter = BookmarksAdapter(requireContext(), bookmarksStorage!!, ::bookmarkSelected, ::bookmarkEdit, folder?.parent)
                folder = folder?.parent
            } else {
                folder = null
                toolbar?.title = context?.getString(R.string.bookmarks)
                listview?.adapter = BookmarksAdapter(requireContext(), bookmarksStorage!!, ::bookmarkSelected, ::bookmarkEdit)
            }
        }
        return true
    }

    private fun bookmarkEdit(bookmark: BookmarkItemV2) {
        if (this.bookmarksStorage != null) {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.container, BookmarksEditFragment(bookmark, bookmarksStorage!!.root()), tag)
                addToBackStack(tag)
                commit()
            }
        }
    }

    private fun bookmarkSelected(item: BookmarkItemV2, private: Boolean = false) {
        if (item.type == BookmarkItemV2.BookmarkType.BOOKMARK) {
            requireContext().components.useCases.tabsUseCases.addTab.invoke(item.url!!, selectTab = true, private = private)
            this.closeBookmarks()
        } else {
            folder = item
            toolbar?.title = item.title
            listview?.adapter = BookmarksAdapter(requireContext(), bookmarksStorage!!, ::bookmarkSelected, ::bookmarkEdit, item)
        }
    }

    private fun closeBookmarks() {
        folder = null
        (activity as BrowserActivity).showBrowserFragment()
        bookmarksClosedCallback?.invoke()
    }

    private fun storageChanged() {
        adapter?.notifyDataSetChanged()
        if (bookmarksStorage == null || bookmarksStorage!!.count() == 0) {
            listview!!.visibility = View.GONE
            layoutNoResult!!.visibility = View.VISIBLE
        } else {
            listview!!.visibility = View.VISIBLE
            layoutNoResult!!.visibility = View.GONE
        }
    }
}
