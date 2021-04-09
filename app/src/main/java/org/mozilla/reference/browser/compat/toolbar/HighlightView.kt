package org.mozilla.reference.browser.compat.toolbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import mozilla.components.concept.toolbar.Toolbar
import org.mozilla.reference.browser.R


internal class HighlightView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        visibility = GONE
    }

    var state: Toolbar.Highlight = Toolbar.Highlight.NONE
        set(value) {
            if (value != field) {
                field = value
                updateIcon()
            }
        }

    @VisibleForTesting
    internal var highlightTint: Int? = null

    private var highlightIcon: Drawable =
            requireNotNull(AppCompatResources.getDrawable(context, DEFAULT_ICON))

    fun setTint(tint: Int) {
        highlightTint = tint
        setColorFilter(tint)
    }

    fun setIcon(icons: Drawable) {
        this.highlightIcon = icons

        updateIcon()
    }

    @Synchronized
    @VisibleForTesting
    internal fun updateIcon() {
        val update = state.toUpdate()

        isVisible = update.visible

        contentDescription = if (update.contentDescription != null) {
            context.getString(update.contentDescription)
        } else {
            null
        }

        highlightTint?.let { setColorFilter(it) }
        setImageDrawable(update.drawable)
    }

    companion object {
        const val DEFAULT_ICON = R.drawable.mozac_dot_notification
    }

    private fun Toolbar.Highlight.toUpdate(): Update = when (this) {
        Toolbar.Highlight.AUTOPLAY_BLOCKED -> Update(
                highlightIcon,
                R.string.mozac_browser_toolbar_content_description_autoplay_blocked,
                true)

        Toolbar.Highlight.NONE -> Update(
                null,
                null,
                false
        )
    }
}