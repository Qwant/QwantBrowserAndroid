package org.mozilla.reference.browser.storage

import android.content.Context
import android.util.Log
import mozilla.components.browser.session.Session
import java.io.*

class BookmarksStorage(private var context: Context) {
    private var bookmarksList: ArrayList<BookmarkItem> = arrayListOf()
    private var onChangeCallbacks: ArrayList<() -> Unit> = arrayListOf()

    fun addBookmark(item: BookmarkItem) {
        this.bookmarksList.add(item)
        this.emitOnChange()
    }

    fun addBookmark(session: Session?) {
        if (session != null) {
            if (session.icon != null) this.addBookmark(BookmarkItem(session.title, session.url, SerializableBitmap(session.icon!!)))
            else this.addBookmark(BookmarkItem(session.title, session.url))
        }
    }

    fun deleteBookmark(item: BookmarkItem) {
        this.bookmarksList.remove(item)
        this.emitOnChange()
    }

    fun deleteBookmark(session: Session?) {
        if (session != null) {
            this.bookmarksList.forEach {
                if (it.url == session.url) {
                    this.deleteBookmark(it)
                    return
                }
            }
        }
    }

    fun getBookmarks(): ArrayList<BookmarkItem> { return this.bookmarksList }
    fun count(): Int { return this.bookmarksList.size }
    fun get(i: Int): BookmarkItem { return this.bookmarksList[i] }

    fun persist() {
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(QWANT_BOOKMARKS_FILENAME, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(this.bookmarksList)
            objectOutputStream.flush()
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restore() {
        try {
            val fileInputStream: FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            this.bookmarksList = objectInputStream.readObject() as ArrayList<BookmarkItem>
            objectInputStream.close()
            fileInputStream.close()
            this.emitOnChange()
        } catch (e: IOException) {
            Log.e("QWANT_BROWSER", "Failed reading bookmarks file: IO exception: " + e.message)
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            Log.e("QWANT_BROWSER", "Failed reading bookmarks file: Class not found: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("QWANT_BROWSER", "Failed reading bookmarks file: " + e.message)
            e.printStackTrace()
        }
    }

    fun contains(url: String): Boolean {
        bookmarksList.forEach {
            if (it.url == url)
                return true
        }
        return false
    }

    companion object {
        const val QWANT_BOOKMARKS_FILENAME = "qwant_bookmarks"
    }

    fun onChange(callback: () -> Unit) {
        this.onChangeCallbacks.add(callback)
    }

    private fun emitOnChange() {
        this.onChangeCallbacks.forEach {
            it.invoke()
        }
    }
}