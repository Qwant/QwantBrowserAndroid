/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.tabs

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.view.ToggleImageButton

class TabsPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.Toolbar(context, attrs) {
    private var button: ToggleImageButton
    private lateinit var privateButton: ToggleImageButton
    private var tabsFeature: TabsFeature? = null
    private var isPrivateTray = false
    private var closeTabsTray: (() -> Unit)? = null
    private var callbackTogglePrivacy: ((isPrivate: Boolean) -> Unit)? = null

    init {
        /* navigationContentDescription = "back"
        setNavigationIcon(R.drawable.mozac_ic_back)
        setNavigationOnClickListener {
            closeTabsTray?.invoke()
        } */

        inflateMenu(R.menu.tabstray_menu)
        setOnMenuItemClickListener {
            val tabsUseCases = components.useCases.tabsUseCases
            when (it.itemId) {
                /* R.id.newTab -> {
                    when (isPrivateTray) {
                        true -> tabsUseCases.addPrivateTab.invoke(context.getString(R.string.homepage), selectTab = true) // TODO move to variable
                        false -> tabsUseCases.addTab.invoke(context.getString(R.string.homepage), selectTab = true)
                    }
                    closeTabsTray?.invoke()
                } */
                R.id.closeTab -> {
                    tabsUseCases.removeAllTabsOfType.invoke(private = isPrivateTray)
                }
            }
            true
        }

        button = ToggleImageButton(context).apply {
            id = R.id.button_tabs
            contentDescription = "Tabs"
            setImageDrawable(resources.getThemedDrawable(R.drawable.mozac_ic_tab))
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    updateToggleStates(this, privateButton, false)
                }
            }
        }
        privateButton = ToggleImageButton(context).apply {
            id = R.id.button_private_tabs
            contentDescription = "Private tabs"
            setImageDrawable(resources.getThemedDrawable(R.drawable.mozac_ic_private_browsing))
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    updateToggleStates(this, button, true)
                }
            }
        }

        addView(button)
        addView(privateButton)
    }

    private fun updateToggleStates(ours: ToggleImageButton, theirs: ToggleImageButton, isPrivate: Boolean) {
        // Tint our button
        // ours.drawable.colorTint(if (isPrivate) photonPurple50 else context.theme.resolveAttribute(R.color.menu_items_selected))
        ours.drawable.colorTint(context.theme.resolveAttribute(R.attr.qwant_color_selected))

        // Uncheck their button and remove tint
        theirs.isChecked = false
        theirs.drawable.colorFilter = null

        // Store the state for the menu option
        isPrivateTray = isPrivate

        // Update the tabs tray with our filter
        tabsFeature?.filterTabs { it.private == isPrivate }

        // Update the menu option text
        menu.findItem(R.id.closeTab).title = if (isPrivate) {
            context.getString(R.string.menu_action_close_tabs_private)
        } else {
            context.getString(R.string.menu_action_close_tabs)
        }

        this.callbackTogglePrivacy?.invoke(isPrivate)
    }

    fun onTogglePrivacy(callback: (isPrivate: Boolean) ->  Unit) {
        this.callbackTogglePrivacy = callback
    }

    fun initialize(isPrivate: Boolean, tabsFeature: TabsFeature?, closeTabsTray: () -> Unit) {
        this.tabsFeature = tabsFeature
        this.closeTabsTray = closeTabsTray
        this.isPrivateTray = isPrivate

        button.isChecked = !isPrivate
        privateButton.isChecked = isPrivate
    }

    private fun Resources.getThemedDrawable(@DrawableRes resId: Int) = getDrawable(resId, context.theme)

    private fun Drawable.colorTint(@ColorRes color: Int) = apply {
        mutate()
        setColorFilter(ContextCompat.getColor(context, color), SRC_IN)
    }

    private val components = context.components
}
