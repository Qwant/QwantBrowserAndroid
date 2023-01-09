package org.mozilla.reference.browser.storage.bookmarks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import mozilla.components.browser.state.state.TabSessionState
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.storage.BookmarkItem
import org.mozilla.reference.browser.storage.BookmarkItemV1
import org.mozilla.reference.browser.storage.BookmarkItemV2
import org.mozilla.reference.browser.storage.SerializableBitmap
import java.io.*

@Suppress("UNCHECKED_CAST")
class BookmarksStorage(private var context: Context) {
    private var bookmarksList: ArrayList<BookmarkItemV2> = arrayListOf()
    private var onChangeCallbacks: ArrayList<() -> Unit> = arrayListOf()

    fun root(): ArrayList<BookmarkItemV2> {
        return bookmarksList
    }

    fun addBookmark(item: BookmarkItemV2) {
        if (item.parent != null) {
            item.parent?.addChild(item)
        } else {
            this.bookmarksList.add(item)
        }
        this.emitOnChange()
        this.persist()
    }

    fun addBookmark(session: TabSessionState?) {
        if (session != null) {
            val content = session.content
            if (content.icon != null) this.addBookmark(BookmarkItemV2(BookmarkItemV2.BookmarkType.BOOKMARK, content.title, content.url, SerializableBitmap(content.icon!!)))
            else this.addBookmark(BookmarkItemV2(BookmarkItemV2.BookmarkType.BOOKMARK, content.title, content.url))
            Toast.makeText(context, context.getString(R.string.bookmarks_added), Toast.LENGTH_LONG).show()
        }
    }

    fun deleteBookmark(item: BookmarkItemV2) {
        if (item.parent != null) {
            item.parent?.removeChild(item)
        } else {
            this.bookmarksList.remove(item)
        }

        this.persist()
        this.emitOnChange()
        Toast.makeText(context, context.getString(R.string.bookmarks_deleted), Toast.LENGTH_LONG).show()
    }

    fun deleteBookmark(session: TabSessionState?) {
        if (session != null) {
            val b = this.getBookmark(session.content.url)
            if (b != null) this.deleteBookmark(b)
        }
    }

    private fun getBookmark(url: String, list: ArrayList<BookmarkItemV2>? = null) : BookmarkItemV2? {
        (list ?: this.bookmarksList).forEach {
            if (it.type == BookmarkItemV2.BookmarkType.BOOKMARK) {
                if (it.url == url) return it
            } else {
                val b = getBookmark(url, it.children)
                if (b != null) return b
            }
        }
        return null
    }

    private fun hasBookmark(url: String, list: ArrayList<BookmarkItemV2>? = null) : Boolean {
        (list ?: this.bookmarksList).forEach {
            if (it.type == BookmarkItemV2.BookmarkType.BOOKMARK) {
                if (it.url == url) return true
            } else {
                if (it.children.isNotEmpty()) {
                    if (hasBookmark(url, it.children)) return true
                }
            }
        }
        return false
    }

    // fun getBookmarks(): ArrayList<BookmarkItemV2> { return this.bookmarksList }
    fun count(): Int { return this.bookmarksList.size }
    fun get(i: Int): BookmarkItemV2 { return this.bookmarksList[i] }

    fun persist() {
        try {
            Log.d("QWANT_BROWSER", "persist bookmarks")
            val fileOutputStream: FileOutputStream = context.openFileOutput(QWANT_BOOKMARKS_FILENAME, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(this.bookmarksList)
            objectOutputStream.flush()
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            Log.d("QWANT_BROWSER", "persist bookmarks error: " + e.message)
            e.printStackTrace()
        }
    }

    private fun doRestoreOldOld() {
        try {
            val f = File(QWANT_BOOKMARKS_FILENAME)
            if (f.exists() && f.canRead()) {
                val fileInputStream = FileInputStream(f) // : FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val oldBookmarks: ArrayList<BookmarkItem> = objectInputStream.readObject() as ArrayList<BookmarkItem>
                objectInputStream.close()
                fileInputStream.close()

                oldBookmarks.forEach {
                    this.bookmarksList.add(BookmarkItemV2(BookmarkItemV2.BookmarkType.BOOKMARK, it.title, it.url))
                }

                this.persist()
                this.emitOnChange()
            } else {
                Log.w("QWANT_BROWSER", "no bookmarks to restore")
            }
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

    private fun doRestoreOld() {
        try {
            val f = File(QWANT_BOOKMARKS_FILENAME)
            if (f.exists() && f.canRead()) {
                val fileInputStream = FileInputStream(f) // : FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val oldBookmarks: ArrayList<BookmarkItemV1> = objectInputStream.readObject() as ArrayList<BookmarkItemV1>
                objectInputStream.close()
                fileInputStream.close()

                oldBookmarks.forEach {
                    this.bookmarksList.add(BookmarkItemV2(BookmarkItemV2.BookmarkType.BOOKMARK, it.title, it.url))
                }

                this.persist()
                this.emitOnChange()
            } else {
                Log.w("QWANT_BROWSER", "no bookmarks to restore")
            }
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

    private fun doRestore() {
        try {
            val file: File = context.getFileStreamPath(QWANT_BOOKMARKS_FILENAME)
            if (file.exists()) {
                val fileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME) // FileInputStream(f)
                val objectInputStream = ObjectInputStream(fileInputStream)
                this.bookmarksList = objectInputStream.readObject() as ArrayList<BookmarkItemV2>

                // reload parents, ignored in serialization else we would go infinite recursion
                this.bookmarksList.filter { it.type == BookmarkItemV2.BookmarkType.FOLDER }.forEach {
                    restoreBookmarksParents(it)
                }

                objectInputStream.close()
                fileInputStream.close()
                this.emitOnChange()
            } else {
                Log.w("QWANT_BROWSER" ,"No bookmarks yet")
            }
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

    private fun restoreBookmarksParents(folder :BookmarkItemV2) {
        folder.children?.forEach {
            if (it.type == BookmarkItemV2.BookmarkType.FOLDER) {
                it.parent = folder
                restoreBookmarksParents(it)
            } else { it.parent = folder }
        }
    }

    fun restore() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val prefkey = context.resources.getString(R.string.pref_key_bookmarks_version)
        val bookmarksVersion = prefs.getInt(prefkey, 0)

        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putInt(prefkey, 2)
        editor.apply()

        when (bookmarksVersion) {
            0 -> doRestoreOldOld()
            1 -> doRestoreOld()
            else -> doRestore()
        }
    }

    fun contains(url: String): Boolean {
        return this.hasBookmark(url)
    }

    companion object {
        const val QWANT_BOOKMARKS_FILENAME = "qwant_bookmarks"
    }

    fun onChange(callback: () -> Unit) {
        this.onChangeCallbacks.add(callback)
    }

    private fun emitOnChange() {
        this.onChangeCallbacks.forEach { it.invoke() }
    }
}