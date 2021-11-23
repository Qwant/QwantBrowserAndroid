package org.mozilla.reference.browser.tabs

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.thumbnail.TabThumbnailView
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.concept.tabstray.Tab
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R

class TabListItemRecycleViewHolder(
        item_layout: View,
        private val context: Context,
        private val thumbnailLoader: ThumbnailLoader,
        private val selectedCallback: (tab: Tab?) -> Unit,
        private val deletedCallback: (tab: Tab?) -> Unit
) : RecyclerView.ViewHolder(item_layout) {
    private var itemMainLayout: LinearLayout = item_layout.findViewById(R.id.tablist_item_layout)
    private var itemPreview: TabThumbnailView = item_layout.findViewById(R.id.tablist_item_preview)
    private var itemTitle: TextView = item_layout.findViewById(R.id.tablist_item_title)
    private var itemUrl: TextView = item_layout.findViewById(R.id.tablist_item_url)
    private var itemDelete: AppCompatImageButton = item_layout.findViewById(R.id.tablist_item_delete)

    fun bind(tab: Tab, isSelected: Boolean) {
        // val tab = tabSession.toTab()

        val title = if (tab.title.length > MAX_TITLE_LENGTH) tab.title.substring(0, MAX_TITLE_LENGTH - 3) + "..." else tab.title
        val url = if (tab.url.length > MAX_URL_LENGTH) tab.url.substring(0, MAX_URL_LENGTH - 3) + "..." else tab.url

        this.itemTitle.text = title
        this.itemUrl.text = url
        this.itemMainLayout.setOnClickListener { selectedCallback.invoke(tab) }
        this.itemDelete.setOnClickListener { deletedCallback.invoke(tab) }

        this.setThumbnail(tab)
        this.setSelected(isSelected)
    }

    private fun setSelected(isSelected: Boolean) {
        if (isSelected) {
            this.itemMainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.qwant_tabs_selected))
        } else {
            this.itemMainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.qwant_background))
        }
    }

    private fun setThumbnail(tab: Tab) {
        if (tab.thumbnail == null) {
            val placeholder: Drawable? = tab.icon?.toDrawable(context.resources) ?: ContextCompat.getDrawable(context, R.drawable.default_favicon)
            val thumbnailSize = 100.dpToPx(this.itemPreview.context.resources.displayMetrics)
            thumbnailLoader.loadIntoView(
                    this.itemPreview,
                    ImageLoadRequest(id = tab.id, size = thumbnailSize),
                    placeholder
            )
        } else if (tab.thumbnail != null) {
            this.itemPreview.setImageBitmap(tab.thumbnail)
        }
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 45
        private const val MAX_URL_LENGTH = 40
    }
}