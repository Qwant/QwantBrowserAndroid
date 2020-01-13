package org.mozilla.reference.browser.storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mozilla.components.support.base.feature.UserInteractionHandler

class BookmarksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            with(supportFragmentManager.beginTransaction()) {
                replace(android.R.id.content, BookmarksFragment())
                commit()
            }
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            } else {
                super.onBackPressed()
            }
        }
    }
}
