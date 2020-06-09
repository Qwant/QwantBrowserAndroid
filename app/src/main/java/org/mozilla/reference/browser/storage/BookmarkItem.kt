package org.mozilla.reference.browser.storage

import java.io.Serializable

class BookmarkItem(val title: String, val url: String) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 4457589957286554918
    }
}
