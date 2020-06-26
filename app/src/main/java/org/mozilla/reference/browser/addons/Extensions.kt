/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import java.text.NumberFormat
import java.util.Locale
import androidx.fragment.app.Fragment

/**
 * Try to find the default language on the map otherwise defaults to "en-US".
 */
internal fun Map<String, String>.translate(): String {
    val lang = Locale.getDefault().isO3Language
    return get(lang) ?: get("en-US") ?: asIterable().firstOrNull()?.value ?: ""
}

internal fun getFormattedAmount(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(amount)
}


/**
 * Run the [block] only if the [Fragment] is attached.
 *
 * @param block A callback to be executed if the container [Fragment] is attached.
 */
internal inline fun Fragment.runIfFragmentIsAttached(block: () -> Unit) {
    context?.let {
        block()
    }
}
