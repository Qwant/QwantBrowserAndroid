/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.storage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment

/**
 * A fragment for displaying the tabs tray.
 */
class BookmarksFragment(
        var bookmarksStorage: BookmarksStorage,
        val bookmarksClosedCallback: (() -> Unit)? = null
) : Fragment(), UserInteractionHandler {
    private var adapter: BookmarksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        val listview: ListView = view.findViewById(R.id.bookmarks_listview)
        adapter = BookmarksAdapter(this.context!!, bookmarksStorage)
        bookmarksStorage.onChange(::storageChanged)
        listview.adapter = adapter

        return view
    }

    override fun onBackPressed(): Boolean {
        this.closeBookmarks()
        return true
    }

    private fun closeBookmarks() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
        bookmarksClosedCallback?.invoke()
    }

    private fun storageChanged() {
        adapter?.notifyDataSetChanged()
    }
}
