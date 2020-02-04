package org.mozilla.reference.browser.view

import mozilla.components.browser.menu.item.BrowserMenuCompoundButton
import org.mozilla.reference.browser.R

/**
 * A simple browser menu switch.
 *
 * @param label The visible label of this menu item.
 * @param initialState The initial value the checkbox should have.
 * @param listener Callback to be invoked when this menu item is checked.
 */
class BrowserMenuSwitch(
        label: String,
        initialState: () -> Boolean = { false },
        listener: (Boolean) -> Unit
) : BrowserMenuCompoundButton(label, initialState, listener) {
    override fun getLayoutResource(): Int = R.layout.browser_menu_item_switch
}
