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
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.ui.colors.R.color.photonPurple50
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.application
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
    private var currentTheme = R.style.ThemeQwantNoActionBar

    init {
        /* navigationContentDescription = "back"
        setNavigationIcon(R.drawable.mozac_ic_back)
        setNavigationOnClickListener {
            closeTabsTray?.invoke()
        } */
        isSaveEnabled = true

        inflateMenu(R.menu.tabstray_menu)
        setOnMenuItemClickListener {
            val tabsUseCases = components.useCases.tabsUseCases
            when (it.itemId) {
                R.id.newTab -> {
                    when (isPrivateTray) {
                        true -> tabsUseCases.addPrivateTab.invoke(context.getString(R.string.homepage), selectTab = true) // TODO move to variable
                        false -> tabsUseCases.addTab.invoke(context.getString(R.string.homepage), selectTab = true)
                    }
                    closeTabsTray?.invoke()
                }
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

        val theme = if (isPrivate) R.style.ThemeQwantNoActionBarPrivacy else R.style.ThemeQwantNoActionBar
        if (theme != currentTheme) {
            with (PreferenceManager.getDefaultSharedPreferences(context).edit()) {
                putInt("theme", theme)
                commit()
            }
            context.application.currentActivity?.recreate()
        }
    }

    /* private class State(
            var isPrivate: Boolean
    ) : Parcelable {

        constructor(): this(false)
        constructor(parcel: Parcel) : this() {
            isPrivate = (parcel.readInt() == 1)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(if (this.isPrivate) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State {
                return State(parcel)
            }

            override fun newArray(size: Int): Array<State?> {
                return arrayOfNulls(size)
            }
        }

    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.d("QWANT_BROWSER", "tabs restore")
        super.onRestoreInstanceState(state)
        if (state is State) {
            isPrivateTray = state.isPrivate
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        return State(this.isPrivateTray)
    } */

    fun initialize(tabsFeature: TabsFeature?, closeTabsTray: () -> Unit) {
        Log.d("QWANT_BROWSER", "tabs init")
        this.tabsFeature = tabsFeature
        this.closeTabsTray = closeTabsTray

        // val currentSession = context.components.core.sessionManager.selectedSession
        // button.isChecked = (currentSession == null || !currentSession.private)
        // privateButton.isChecked = (currentSession != null && currentSession.private)

        button.isChecked = !isPrivateTray
        privateButton.isChecked = isPrivateTray
    }

    private fun Resources.getThemedDrawable(@DrawableRes resId: Int) = getDrawable(resId, context.theme)

    private fun Drawable.colorTint(@ColorRes color: Int) = apply {
        mutate()
        setColorFilter(ContextCompat.getColor(context, color), SRC_IN)
    }

    private val components = context.components
}
