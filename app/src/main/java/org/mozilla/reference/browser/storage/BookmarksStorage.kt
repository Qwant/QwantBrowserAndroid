package org.mozilla.reference.browser.storage

import android.content.Context
import android.util.Log
import java.io.*

class BookmarksStorage(private var context: Context) {
    private var bookmarksList: ArrayList<BookmarkItem> = arrayListOf()
    private var onChangeCallbacks: ArrayList<() -> Unit> = arrayListOf()

    init {
        this.restore()
        bookmarksList.add(BookmarkItem("test", "test url", "fav"))
        bookmarksList.add(BookmarkItem("test 2", "test url 2", "fav"))
        bookmarksList.add(BookmarkItem("test 3", "test url 3", "fav"))
        this.emitOnChange()
    }

    fun addBookmark(item: BookmarkItem) {
        this.bookmarksList.add(item)
        this.emitOnChange()
    }

    fun removeBookmark(item: BookmarkItem) {
        this.bookmarksList.remove(item)
        this.emitOnChange()
    }

    fun getBookmarks(): ArrayList<BookmarkItem> {
        return this.bookmarksList
    }

    fun count(): Int { return this.bookmarksList.size }
    fun get(i: Int): BookmarkItem { return this.bookmarksList[i] }

    fun persist() {
        Log.d("QWANT_BROWSER", "save bookmarks !")
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(QWANT_BOOKMARKS_FILENAME, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(this.bookmarksList)
            objectOutputStream.flush()
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            Log.e("QWANT_BROWSER", "Failed to save history: " + e.message)
            e.printStackTrace()
        }
    }

    fun restore() {
        Log.d("QWANT_BROWSER", "restore bookmarks !")
        try {
            val fileInputStream: FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            this.bookmarksList = objectInputStream.readObject() as java.util.ArrayList<BookmarkItem>
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