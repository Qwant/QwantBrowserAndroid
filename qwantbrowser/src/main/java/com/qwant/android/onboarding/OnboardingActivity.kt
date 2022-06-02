package com.qwant.android.onboarding

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R


class OnboardingActivity : AppCompatActivity() {
    enum class OnboardingPage {
        ANONYMOUS, NAVIGATION
    }

    var currentPage = OnboardingPage.ANONYMOUS
    var shouldShowNavigationOnResume = false

    lateinit var defaultBrowserLauncher: ActivityResultLauncher<Intent>

    lateinit var layout: ConstraintLayout
    lateinit var image: ImageView
    lateinit var title: TextView
    lateinit var bullet1: TextView
    lateinit var bullet2: TextView
    lateinit var bullet3: TextView
    lateinit var textTop: TextView
    lateinit var textBottom: TextView
    lateinit var buttonValidate: Button
    lateinit var buttonMore: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_layout)

        defaultBrowserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d("QWANT_BROWSER_ONBOARD", "Default browser changed")
            } else {
                Log.d("QWANT_BROWSER_ONBOARD", "Default browser change ignored")
            }
            initOnboardingForNavigation()
        }

        layout = findViewById(R.id.onboarding_layout)
        image = findViewById(R.id.onboarding_image)
        title = findViewById(R.id.onboarding_title)
        bullet1 = findViewById(R.id.onboarding_bullet_1)
        bullet2 = findViewById(R.id.onboarding_bullet_2)
        bullet3 = findViewById(R.id.onboarding_bullet_3)
        textTop = findViewById(R.id.onboarding_text_top)
        textBottom = findViewById(R.id.onboarding_text_bottom)
        buttonValidate = findViewById(R.id.onboarding_validate)
        buttonMore = findViewById(R.id.onboarding_more)

        this.initOnboardingForAnonymous()
    }

    fun initOnboardingForAnonymous() {
        currentPage = OnboardingPage.ANONYMOUS

        layout.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.qwant_purple_light_v2, theme))

        image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.onboarding_anonyme_2_x))

        title.text = getString(R.string.onboarding_anonymous_title)
        textTop.apply {
            text = getString(R.string.onboarding_anonymous_text_top)
            visibility = View.VISIBLE
        }
        bullet1.text = getString(R.string.onboarding_anonymous_bullet_1)
        bullet2.text = getString(R.string.onboarding_anonymous_bullet_2)
        bullet3.apply {
            text = getString(R.string.onboarding_anonymous_bullet_3)
            visibility = View.VISIBLE
        }
        textBottom.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Showing default browser popup
            buttonValidate.apply {
                visibility = View.VISIBLE
                text = getString(R.string.onboarding_anonymous_validate)
                setOnClickListener {
                    val roleManager = context.getSystemService(RoleManager::class.java)
                    val isRoleAvailable = roleManager?.isRoleAvailable(RoleManager.ROLE_BROWSER) ?: false
                    if (isRoleAvailable) {
                        val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
                        if (!isRoleHeld) {
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                            defaultBrowserLauncher.launch(intent)
                        } else initOnboardingForNavigation()
                    } else initOnboardingForNavigation()
                }
            }
            buttonMore.visibility = View.GONE
        } else {
            // Sending to default browser system settings (or skip)
            buttonValidate.apply {
                visibility = View.VISIBLE
                text = getString(R.string.settings_general_makedefaultbrowser)
                setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        startActivity(intent)
                        shouldShowNavigationOnResume = true
                    } else {
                        Toast.makeText(context, "Android version too old", Toast.LENGTH_LONG).show()
                        Log.e("QWANT_BROWSER", "Android version to old")
                    }
                }
            }
            buttonMore.apply {
                visibility = View.VISIBLE
                text = getString(R.string.onboarding_anonymous_validate)
                setOnClickListener {
                    initOnboardingForNavigation()
                }
            }
        }
    }

    fun initOnboardingForNavigation() {
        currentPage = OnboardingPage.NAVIGATION

        layout.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.qwant_green_light_v2, theme))

        image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.onboarding_navigation_2_x))

        title.text = getString(R.string.onboarding_navigation_title)
        textTop.visibility = View.GONE
        bullet1.text = getString(R.string.onboarding_navigation_bullet_1)
        bullet2.text = getString(R.string.onboarding_navigation_bullet_2)
        bullet3.visibility = View.GONE
        textBottom.apply {
            text = getString(R.string.onboarding_navigation_text_bottom)
            visibility = View.VISIBLE
        }

        buttonValidate.apply {
            visibility = View.VISIBLE
            text = getString(R.string.onboarding_navigation_validate)
            setOnClickListener {
                val prefkey = resources.getString(R.string.pref_key_show_onboarding)
                val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val prefEditor = prefs.edit()
                prefEditor.putBoolean(prefkey, false)
                prefEditor.apply()

                finish()
            }
        }
        buttonMore.visibility = View.GONE
    }

    override fun onResume() {
        if (shouldShowNavigationOnResume || currentPage == OnboardingPage.NAVIGATION) {
            shouldShowNavigationOnResume = false
            initOnboardingForNavigation()
        }
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("onboarding_page", currentPage.ordinal)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val page = savedInstanceState.getInt("onboarding_page", OnboardingPage.ANONYMOUS.ordinal)
        if (page == OnboardingPage.NAVIGATION.ordinal) {
            initOnboardingForNavigation()
        }
    }

    override fun onBackPressed() {
        if (currentPage == OnboardingPage.NAVIGATION) {
            initOnboardingForAnonymous()
            return
        }
        this.closeOnboardingAndQuit()
    }

    private fun closeOnboardingAndQuit() {
        val intent = Intent(this, BrowserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.action = "CLOSE_APP"
        startActivity(intent)
    }
}