package org.mozilla.reference.browser.storage.history

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import kotlinx.coroutines.*
import org.mozilla.reference.browser.ext.components
import java.util.*

/**
 * A fragment for displaying the tabs tray.
 */
class HistoryFragment: Fragment(), UserInteractionHandler {
    private var historyClosedCallback: (() -> Unit)? = null
    fun setHistoryClosedCallback(cb: () -> Unit) { this.historyClosedCallback = cb }

    private val mainScope = MainScope()

    private var visits = mutableListOf<HistoryAdapter.HistoryItem>()
    private var adapter: HistoryAdapter? = null

    private var currentLoadedIndex: Long = 0

    private var listview: ListView? = null
    private var clearAll: ImageButton? = null
    private var layoutNoResult: LinearLayout? = null
    private var topbar: Toolbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        loadVisits(0, HISTORY_PAGE_SIZE, false)
        currentLoadedIndex = HISTORY_PAGE_SIZE

        val view: View = inflater.inflate(R.layout.fragment_history, container, false)

        listview = view.findViewById(R.id.history_listview)
        clearAll = view.findViewById(R.id.history_clear_all)
        layoutNoResult = view.findViewById(R.id.history_noresult_layout)
        topbar = view.findViewById(R.id.history_toolbar)

        adapter = HistoryAdapter(requireContext(), ::historyItemSelected, ::reload, ::loadMore)
        listview!!.adapter = adapter

        clearAll?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete history")
                .setMessage("Do you really want to delete all history ?")
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    MainScope().launch {
                        context?.components?.core?.historyStorage?.deleteEverything()
                        reload()
                        Toast.makeText(context, "History have been cleaned up", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.no, null).show()
        }

        topbar?.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.qwant_text))
        topbar?.setNavigationOnClickListener { this.onBackPressed() }

        return view
    }

    private fun loadVisits(min: Long, count: Long, append: Boolean = true) {
        mainScope.launch {
            if (context != null) {
                if (!append) visits = mutableListOf()
                else {
                    val last = visits.last()
                    if (last.isLoadMore) {
                        visits.remove(last)
                    }
                }

                val newVisits = requireContext().components.core.historyStorage.getVisitsPaginated(min, count)
                val calendar = Calendar.getInstance()
                calendar.time = Date()
                val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                val lastItem = visits.findLast { !it.isTitle }
                var lastTitleDateOfYear = if (lastItem?.visit != null) {
                    calendar.time = Date(lastItem.visit.visitTime)
                    calendar.get(Calendar.DAY_OF_YEAR)
                } else {
                    null
                }

                newVisits.forEach {
                    calendar.time = Date(it.visitTime)
                    if (lastTitleDateOfYear == null || calendar.get(Calendar.DAY_OF_YEAR) != lastTitleDateOfYear) {
                        val dateString = when (todayDayOfYear - calendar.get(Calendar.DAY_OF_YEAR)) {
                            0 -> context?.getString(R.string.history_today)
                            1 -> context?.getString(R.string.history_yesterday)
                            else -> calendar.get(Calendar.DAY_OF_MONTH).toString() + "/" + calendar.get(Calendar.MONTH).toString()
                        }
                        visits.add(HistoryAdapter.HistoryItem(null, dateString, true))
                        lastTitleDateOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                    }
                    visits.add(HistoryAdapter.HistoryItem(it))
                }

                if (visits.isEmpty()) {
                    listview?.visibility = View.GONE
                    clearAll?.visibility = View.GONE
                    layoutNoResult?.visibility = View.VISIBLE
                } else {
                    if (newVisits.size == count.toInt()) {
                        visits.add(HistoryAdapter.HistoryItem(null, context?.getString(R.string.history_load_more), isTitle = true, isLoadMore = true))
                    }
                    adapter?.setVisits(visits)
                    listview?.visibility = View.VISIBLE
                    clearAll?.visibility = View.VISIBLE
                    layoutNoResult?.visibility = View.GONE
                }
            }
        }
    }

    private fun loadMore() {
        this.loadVisits(currentLoadedIndex, HISTORY_PAGE_SIZE, true)
        currentLoadedIndex += HISTORY_PAGE_SIZE
    }

    private fun reload() {
        this.loadVisits(0, currentLoadedIndex, false)
    }

    override fun onBackPressed(): Boolean {
        this.closeHistory()
        return true
    }

    private fun historyItemSelected(item: VisitInfo) {
        this.closeHistory()
    }

    private fun closeHistory() {
        val ba = (activity as BrowserActivity)
        ba.showBrowserFragment()
        historyClosedCallback?.invoke()
    }

    companion object {
        private const val HISTORY_PAGE_SIZE: Long = 50
    }
}
