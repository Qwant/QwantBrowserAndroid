package com.qwant.android.webext

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components


class ABPRemovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_layout)
        findViewById<Button>(R.id.onboarding_validate)?.setOnClickListener {
            this.finish()
        }
        findViewById<Button>(R.id.onboarding_more)?.setOnClickListener {
            components.useCases.tabsUseCases.addTab("http://www.qwant.com/extension_more_info", true)
            this.finish()
        }
    }
}