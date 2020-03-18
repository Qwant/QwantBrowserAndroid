package org.mozilla.reference.browser.storage

import java.io.Serializable

class BookmarkItem(val title: String, val url: String, val icon: SerializableBitmap? = null) : Serializable