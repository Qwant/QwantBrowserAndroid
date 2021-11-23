package org.mozilla.reference.browser.storage.bookmarks

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.storage.BookmarkItemV2
import androidx.core.content.ContextCompat.getSystemService




class BookmarksEditFragment(
        private val bookmark: BookmarkItemV2,
        private val bookmarksRoot: ArrayList<BookmarkItemV2>,
) : Fragment(), UserInteractionHandler {
    private var newParent: BookmarkItemV2? = null
    private var parentChanged = false
    private var editingFolder = false

    private var foldersList: ListView? = null
    private var bookmarkForm: LinearLayout? = null

    private var editTitle: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_bookmark_edit, container, false)

        Log.d("QWANT_BROWSER_BOOKMARKS", "oncreateview")

        editTitle = view.findViewById(R.id.bookmark_edit_title)
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

        editTitle?.text?.append(bookmark.title)
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
                if (editTitle?.text?.isEmpty() == true || (bookmark.type == BookmarkItemV2.BookmarkType.BOOKMARK && editUrl.text.isEmpty())) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show() // TODO translations
                } else {
                    bookmark.title = editTitle?.text.toString()
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
        }
        buttonCancel.setOnClickListener { cancel() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("QWANT_BROWSER_BOOKMARKS", "onviewcreated")

        // Focus and show keyboard
        // editTitle?.setSelection(bookmark.title.length)
        if (editTitle?.requestFocus() == true) {
            Log.d("QWANT_BROWSER_BOOKMARKS", "onviewcreated show keyboard")
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTitle, InputMethodManager.SHOW_IMPLICIT)
            Log.d("QWANT_BROWSER_BOOKMARKS", "onviewcreated show done")
        }
    }

    override fun onStart() {
        super.onStart()

        Log.d("QWANT_BROWSER_BOOKMARKS", "onstart")

        // Focus and show keyboard
        // editTitle?.setSelection(bookmark.title.length)
        if (editTitle?.requestFocus() == true) {
            Log.d("QWANT_BROWSER_BOOKMARKS", "onstart show keyboard")
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTitle, InputMethodManager.SHOW_IMPLICIT)
            Log.d("QWANT_BROWSER_BOOKMARKS", "onstart show done")
        }
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