package org.mozilla.reference.browser.browser.icons.utils

import android.view.View
import kotlinx.coroutines.Job

/**
 * Cancels the provided job when a view is detached from the window
 */
internal class CancelOnDetach(private val job: Job) : View.OnAttachStateChangeListener {

    override fun onViewAttachedToWindow(v: View) = Unit
    override fun onViewDetachedFromWindow(v: View) = job.cancel()

}