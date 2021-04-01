package org.mozilla.reference.browser.storage.bookmarks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.storage.BookmarkItemV2

class BookmarksEditFragment(
        val bookmark: BookmarkItemV2,
        private val bookmarksRoot: ArrayList<BookmarkItemV2>
) : Fragment(), UserInteractionHandler {
    private var newParent: BookmarkItemV2? = null
    private var parentChanged = false
    private var editingFolder = false

    private var foldersList: ListView? = null
    private var bookmarkForm: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_bookmark_edit, container, false)

        val editTitle: EditText = view.findViewById(R.id.bookmark_edit_title)
        val editUrl: EditText = view.findViewById(R.id.bookmark_edit_url)
        val editUrlLabel: AppCompatTextView = view.findViewById(R.id.bookmark_edit_url_label)
        val editFolder: EditText = view.findViewById(R.id.bookmark_edit_folder)
        val buttonCancel: Button = view.findViewById(R.id.bookmark_edit_button_cancel)
        val buttonSave: Button = view.findViewById(R.id.bookmark_edit_button_save)
        foldersList = view.findViewById(R.id.folders_list)
        bookmarkForm = view.findViewById(R.id.bookmark_form)

        val folderAdapter = BookmarksFoldersAdapter(requireContext(), bookmark, bookmarksRoot)
        foldersList?.adapter = folderAdapter
        foldersList?.dividerHeight = 0

        val rootTitle = context?.getString(R.string.bookmarks)

        editTitle.text.append(bookmark.title)
        editTitle.setSelection(0)
        editFolder.text.append(bookmark.parent?.title ?: rootTitle)
        editFolder.setSelection(0)
        if (bookmark.type == BookmarkItemV2.BookmarkType.FOLDER) {
            editUrlLabel.visibility = View.GONE
            editUrl.visibility = View.GONE
        } else {
            editUrlLabel.visibility = View.VISIBLE
            editUrl.visibility = View.VISIBLE
            editUrl.text.append(bookmark.url)
            editUrl.setSelection(0)
        }

        editFolder.setOnClickListener {
            folderAdapter.reset()
            foldersList?.visibility = View.VISIBLE
            bookmarkForm?.visibility = View.GONE
            editingFolder = true
        }

        buttonSave.setOnClickListener {
            if (editingFolder) {
                val selectedFolder = folderAdapter.getSelectedBookmark()
                if (selectedFolder !== bookmark.parent) {
                    parentChanged = true
                    newParent = folderAdapter.getSelectedBookmark()
                    editFolder.text.clear()
                    editFolder.text.append(newParent?.title ?: rootTitle)
                    editFolder.setSelection(0)
                }
                foldersList?.visibility = View.GONE
                bookmarkForm?.visibility = View.VISIBLE
                editingFolder = false
            } else {
                bookmark.title = editTitle.text.toString()
                bookmark.url = editUrl.text.toString()
                if (parentChanged) {
                    // remove from parent or root
                    if (bookmark.parent == null) {
                        bookmarksRoot.remove(bookmark)
                    } else {
                        bookmark.parent?.removeChild(bookmark)
                    }
                    // add to parent or root
                    if (newParent == null) {
                        bookmarksRoot.add(bookmark)
                    } else {
                        newParent?.addChild(bookmark)
                    }
                    // change child parent
                    bookmark.parent = newParent
                }
                setFragmentResult("EditBookmarkResult", bundleOf("bookmark_updated" to true))
                parentFragmentManager.popBackStackImmediate()
            }
        }
        buttonCancel.setOnClickListener { cancel() }

        return view
    }

    override fun onBackPressed(): Boolean {
        cancel()
        return true
    }

    private fun cancel() {
        if (editingFolder) {
            foldersList?.visibility = View.GONE
            bookmarkForm?.visibility = View.VISIBLE
            editingFolder = false
        } else {
            setFragmentResult("EditBookmarkResult", bundleOf("bookmark_updated" to false))
            parentFragmentManager.popBackStackImmediate()
        }
    }
}