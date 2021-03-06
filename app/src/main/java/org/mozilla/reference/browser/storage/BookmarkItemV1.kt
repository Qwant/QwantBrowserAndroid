package org.mozilla.reference.browser.storage

import java.io.Serializable

class BookmarkItemV1(val title: String, val url: String, val icon: SerializableBitmap? = null) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 4457589957286554919
    }
}