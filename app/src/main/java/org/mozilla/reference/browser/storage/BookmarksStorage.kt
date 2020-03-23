package org.mozilla.reference.browser.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import mozilla.components.browser.session.Session
import org.mozilla.reference.browser.R
import java.io.*

class BookmarksStorage(private var context: Context) {
    private var bookmarksList: ArrayList<BookmarkItemV1> = arrayListOf()
    private var onChangeCallbacks: ArrayList<() -> Unit> = arrayListOf()

    fun addBookmark(item: BookmarkItemV1) {
        this.bookmarksList.add(item)
        this.emitOnChange()
    }

    fun addBookmark(session: Session?) {
        if (session != null) {
            if (session.icon != null) this.addBookmark(BookmarkItemV1(session.title, session.url, SerializableBitmap(session.icon!!)))
            else this.addBookmark(BookmarkItemV1(session.title, session.url))
        }
    }

    fun deleteBookmark(item: BookmarkItemV1) {
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

    fun getBookmarks(): ArrayList<BookmarkItemV1> { return this.bookmarksList }
    fun count(): Int { return this.bookmarksList.size }
    fun get(i: Int): BookmarkItemV1 { return this.bookmarksList[i] }

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

    private fun do_restore_old() {
        try {
            val fileInputStream: FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val oldBookmarks: ArrayList<BookmarkItem> = objectInputStream.readObject() as ArrayList<BookmarkItem>
            objectInputStream.close()
            fileInputStream.close()

            oldBookmarks.forEach {
                Log.e("QWANT_BROWSER", "restore old bookmarks: ${it.title} - ${it.url}")
                this.bookmarksList.add(BookmarkItemV1(it.title, it.url))
            }

            this.persist()
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

    private fun do_restore() {
        try {
            val fileInputStream: FileInputStream = context.openFileInput(QWANT_BOOKMARKS_FILENAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            this.bookmarksList = objectInputStream.readObject() as ArrayList<BookmarkItemV1>
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

    fun restore() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val prefkey = context.resources.getString(R.string.pref_key_bookmarks_version)
        val bookmarksVersion = prefs.getInt(prefkey, 0)

        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putInt(prefkey, 1)
        editor.apply()

        if (bookmarksVersion == 0) {
            do_restore_old()
        } else {
            do_restore()
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