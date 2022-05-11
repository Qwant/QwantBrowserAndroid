package com.qwant.android.webext

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.reference.browser.R


class ABPRemovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_layout)
        findViewById<Button>(R.id.onboarding_validate)?.setOnClickListener {
            this.finish()
        }
    }
}