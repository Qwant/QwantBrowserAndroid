package com.qwant.android.webext

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components


class ABPRemovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.onboarding_layout)
        this.initOnboardingForABPRemoval()
    }

    fun initOnboardingForABPRemoval() {
        findViewById<ImageView>(R.id.onboarding_image)?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.adblock_vert_2_x))

        findViewById<TextView>(R.id.onboarding_title)?.text = getString(R.string.onboarding_adblockplus_title)
        findViewById<TextView>(R.id.onboarding_bullet_1)?.text = getString(R.string.onboarding_adblockplus_bullet_1)
        findViewById<TextView>(R.id.onboarding_bullet_2)?.text = getString(R.string.onboarding_adblockplus_bullet_2)

        findViewById<Button>(R.id.onboarding_validate).apply {
            text = getString(R.string.onboarding_adblockplus_validate)
            setOnClickListener {
                finish()
            }
        }
        findViewById<Button>(R.id.onboarding_more)?.apply{
            text = getString(R.string.onboarding_adblockplus_more)
            setOnClickListener {
                components.useCases.tabsUseCases.addTab(getString(R.string.onboarding_adblockplus_more_url), true)
                finish()
            }
        }

        findViewById<TextView>(R.id.onboarding_bullet_3)?.visibility = View.GONE
        findViewById<TextView>(R.id.onboarding_text_top)?.visibility = View.GONE
        findViewById<TextView>(R.id.onboarding_text_bottom)?.visibility = View.GONE
    }
}