package org.mozilla.reference.browser.compat.toolbar

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.StringRes
import mozilla.components.browser.toolbar.facts.ToolbarFacts
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.support.base.Component
import mozilla.components.support.base.facts.Action
import mozilla.components.support.base.facts.Fact
import mozilla.components.support.base.facts.collect
import mozilla.components.ui.autocomplete.InlineAutocompleteEditText

private fun emitToolbarFact(
        action: Action,
        item: String,
        value: String? = null,
        metadata: Map<String, Any>? = null
) {
    Fact(
            Component.BROWSER_TOOLBAR,
            action,
            item,
            value,
            metadata
    ).collect()
}

internal fun emitCommitFact(
        autocompleteResult: InlineAutocompleteEditText.AutocompleteResult?
) {
    val metadata = if (autocompleteResult == null) {
        mapOf(
                "autocomplete" to false
        )
    } else {
        mapOf(
                "autocomplete" to true,
                "source" to autocompleteResult.source
        )
    }

    emitToolbarFact(Action.COMMIT, ToolbarFacts.Items.TOOLBAR, metadata = metadata)
}

internal fun emitOpenMenuFact(extras: Map<String, Any>?) {
    emitToolbarFact(Action.CLICK, ToolbarFacts.Items.MENU, metadata = extras)
}

internal class ActionWrapper(
        var actual: Toolbar.Action,
        var view: View? = null
)
