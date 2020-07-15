package org.mozilla.reference.browser.storage.history

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import kotlinx.coroutines.*
import org.mozilla.reference.browser.ext.components

/**
 * A fragment for displaying the tabs tray.
 */
class HistoryFragment: Fragment(), UserInteractionHandler {
    private var historyClosedCallback: (() -> Unit)? = null
    fun setHistoryClosedCallback(cb: () -> Unit) { this.historyClosedCallback = cb }

    private val mainScope = MainScope()

    private var visits = listOf<VisitInfo>()
    private var adapter: HistoryAdapter? = null

    var listview: ListView? = null
    var layoutNoResult: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_history, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.history_toolbar)
        toolbar.title = context?.getString(R.string.history)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        listview = view.findViewById(R.id.history_listview)
        layoutNoResult = view.findViewById(R.id.history_noresult_layout)

        adapter = HistoryAdapter(context!!, visits, ::historyItemSelected)
        listview!!.adapter = adapter

        loadVisits(0, 100)

        return view
    }

    private fun loadVisits(min: Long, max: Long, append: Boolean = true) {
        mainScope.launch {
            if (context != null) {
                val newVisits = context!!.components.core.historyStorage.getDetailedVisits(min, max)
                visits = if (append) visits + newVisits else newVisits
                storageChanged()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        this.closeHistory()
        return true
    }

    private fun historyItemSelected(item: VisitInfo) {
        this.closeHistory()
    }

    private fun closeHistory() {
        (activity as BrowserActivity).showBrowserFragment()
        historyClosedCallback?.invoke()
    }

    private fun storageChanged() {
        adapter?.notifyDataSetChanged()
        if (visits.isEmpty()) {
            listview!!.visibility = View.GONE
            layoutNoResult!!.visibility = View.VISIBLE
        } else {
            listview!!.visibility = View.VISIBLE
            layoutNoResult!!.visibility = View.GONE
        }
    }
}
