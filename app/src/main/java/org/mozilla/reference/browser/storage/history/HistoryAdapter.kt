package org.mozilla.reference.browser.storage.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuImageText
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import java.text.DecimalFormat
import java.util.*


class HistoryAdapter(
        private val context: Context,
        private val historyItemSelectedCallback: (historyItem: VisitInfo) -> Unit,
        private val reloadCallback: () -> Unit,
        private val loadMoreCallback: () -> Unit
) : BaseAdapter() {
    class HistoryItem (
        val visit: VisitInfo? = null,
        val title: String? = null,
        val isTitle: Boolean = false,
        val isLoadMore: Boolean = false
    )

    private var visits: List<HistoryItem> = listOf()

    internal class HistoryItemViewHolder(
            item_layout: View,
            private val historyItemSelectedCallback: (historyItem: VisitInfo) -> Unit,
            private val reloadCallback: () -> Unit,
            private val loadMoreCallback: () -> Unit,
            private val context: Context
    ) : RecyclerView.ViewHolder(item_layout) {
        private var itemTitle: TextView = item_layout.findViewById(R.id.history_item_title)
        private var itemUrl: TextView = item_layout.findViewById(R.id.history_item_url)
        private var itemHour: TextView = item_layout.findViewById(R.id.history_item_hour)
        private var itemLayoutText: LinearLayout = item_layout.findViewById(R.id.history_item_layout_text)
        private var itemButtonMenu: MenuButton = item_layout.findViewById(R.id.history_item_menu_button)
        private var itemDateTitle: TextView = item_layout.findViewById(R.id.history_item_date_title)

        fun setup(historyItem: HistoryItem) {
            if (historyItem.isTitle) {
                itemHour.visibility = View.GONE
                itemLayoutText.visibility = View.GONE
                itemButtonMenu.visibility = View.GONE
                itemDateTitle.visibility = View.VISIBLE

                itemDateTitle.text = historyItem.title

                if (historyItem.isLoadMore) {
                    this.itemDateTitle.setOnClickListener {
                        loadMoreCallback.invoke()
                    }
                }
            } else if (historyItem.visit != null) {
                itemHour.visibility = View.VISIBLE
                itemLayoutText.visibility = View.VISIBLE
                itemButtonMenu.visibility = View.VISIBLE
                itemDateTitle.visibility = View.GONE

                var title = "NO TITLE"
                if (historyItem.visit.title != null) {
                    title = if (historyItem.visit.title!!.length > MAX_TITLE_LENGTH) historyItem.visit.title!!.substring(0, MAX_TITLE_LENGTH - 3) + "..." else historyItem.visit.title!!
                }
                val url = if (historyItem.visit.url.length > MAX_URL_LENGTH) historyItem.visit.url.substring(0, MAX_URL_LENGTH - 3) + "..." else historyItem.visit.url

                this.itemTitle.text = title
                this.itemUrl.text = url

                val calendar = Calendar.getInstance()
                calendar.time = Date(historyItem.visit.visitTime)
                this.itemHour.text = context.getString(R.string.history_hour_placeholder, calendar.get(Calendar.HOUR_OF_DAY), DecimalFormat("00").format(calendar.get(Calendar.MINUTE)))

                this.itemLayoutText.setOnClickListener {
                    context.components.useCases.sessionUseCases.loadUrl(historyItem.visit.url)
                    historyItemSelectedCallback.invoke(historyItem.visit)
                }

                itemButtonMenu.setColorFilter(ContextCompat.getColor(context, context.theme.resolveAttribute(R.attr.qwant_color_main)))
                itemButtonMenu.menuBuilder = BrowserMenuBuilder(listOf(
                    BrowserMenuImageText(
                            context.getString(R.string.mozac_feature_contextmenu_open_link_in_new_tab),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_ctmenu_newtab
                    ) {
                        context.components.useCases.tabsUseCases.addTab.invoke(historyItem.visit.url, selectTab = true)
                        historyItemSelectedCallback.invoke(historyItem.visit)
                    },
                    BrowserMenuImageText(
                            context.getString(R.string.mozac_feature_contextmenu_open_link_in_private_tab),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_ctmenu_newtab_private
                    ) {
                        context.components.useCases.tabsUseCases.addPrivateTab.invoke(historyItem.visit.url, selectTab = true)
                        historyItemSelectedCallback.invoke(historyItem.visit)
                    },
                    BrowserMenuImageText(
                            context.getString(R.string.mozac_feature_contextmenu_copy_link),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_ctmenu_clipboard
                    ) {
                        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                        val clip = ClipData.newPlainText("Copied URL", historyItem.visit.url)
                        clipboard?.setPrimaryClip(clip)
                    },
                    BrowserMenuImageText(
                            context.getString(R.string.bookmarks_delete),
                            textColorResource = context.theme.resolveAttribute(R.attr.qwant_color_main),
                            imageResource = R.drawable.ic_trash
                    ) {
                        MainScope().launch {
                            context.components.core.historyStorage.deleteVisitsFor(historyItem.visit.url)
                            reloadCallback.invoke()
                        }
                    }
                ))
            }
        }

        companion object {
            private const val MAX_TITLE_LENGTH = 40
            private const val MAX_URL_LENGTH = 45
        }
    }

    fun setVisits(visits: List<HistoryItem>) {
        this.visits = visits
        this.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView: View
        val viewHolder: HistoryItemViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(R.layout.history_list_item, parent, false)
            viewHolder = HistoryItemViewHolder(newView, this.historyItemSelectedCallback, this.reloadCallback, this.loadMoreCallback, this.context)
            newView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as HistoryItemViewHolder
            newView = convertView
        }

        if (position < this.visits.size) {
            viewHolder.setup(this.visits[position])
        }

        return newView
    }

    override fun getItem(position: Int): Any {
        return this.visits[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return this.visits.size
    }
}