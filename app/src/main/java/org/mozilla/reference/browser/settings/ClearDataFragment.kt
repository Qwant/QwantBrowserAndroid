package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.Engine
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class ClearDataFragment(
        private var settingsContainer: SettingsContainerFragment? = null
): Fragment(), UserInteractionHandler {
    private var checkbox_allsites: AppCompatCheckBox? = null
    private var checkbox_cache: AppCompatCheckBox? = null
    private var checkbox_cookies: AppCompatCheckBox? = null
    private var checkbox_sessions: AppCompatCheckBox? = null
    private var checkbox_permissions: AppCompatCheckBox? = null
    private var checkbox_history: AppCompatCheckBox? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_clear_data, container, false)
    }

    fun setSettingsContainerFragment(settingsContainer: SettingsContainerFragment) {
        this.settingsContainer = settingsContainer
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsContainer?.setTitle(getString(R.string.settings_privacy_clear_data))

        checkbox_allsites = view.findViewById(R.id.cleardata_check_allsites)
        checkbox_cache = view.findViewById(R.id.cleardata_check_cache)
        checkbox_cookies = view.findViewById(R.id.cleardata_check_cookies)
        checkbox_sessions = view.findViewById(R.id.cleardata_check_sessions)
        checkbox_permissions = view.findViewById(R.id.cleardata_check_permissions)
        checkbox_history = view.findViewById(R.id.cleardata_check_history)

        checkbox_allsites!!.setOnClickListener {
            if (checkbox_allsites != null && checkbox_cache != null && checkbox_cookies != null && checkbox_sessions != null && checkbox_permissions != null) {
                val isChecked = checkbox_allsites!!.isChecked
                checkbox_cache?.isChecked = isChecked
                checkbox_cookies?.isChecked = isChecked
                checkbox_sessions?.isChecked = isChecked
                checkbox_permissions?.isChecked = isChecked
            }
        }

        val onSubCheckboxListener = View.OnClickListener {
            if (checkbox_allsites != null && checkbox_cache != null && checkbox_cookies != null && checkbox_sessions != null && checkbox_permissions != null
                    && checkbox_cache!!.isChecked && checkbox_cookies!!.isChecked && checkbox_sessions!!.isChecked && checkbox_permissions!!.isChecked) {
                checkbox_allsites!!.isChecked = true
            } else if (checkbox_allsites != null) {
                checkbox_allsites!!.isChecked = false
            }
        }
        checkbox_cache!!.setOnClickListener(onSubCheckboxListener)
        checkbox_cookies!!.setOnClickListener(onSubCheckboxListener)
        checkbox_sessions!!.setOnClickListener(onSubCheckboxListener)
        checkbox_permissions!!.setOnClickListener(onSubCheckboxListener)

        view.findViewById<TextView>(R.id.cleardata_button).setOnClickListener {
            if (checkbox_allsites != null && checkbox_allsites!!.isChecked) {
                context?.components?.core?.engine?.clearData(Engine.BrowsingData.allSiteData())
            } else {
                if (checkbox_cache != null && checkbox_cache!!.isChecked) {
                    context?.components?.core?.engine?.clearData(Engine.BrowsingData.allCaches())
                }
                if (checkbox_cookies != null && checkbox_cookies!!.isChecked) {
                    context?.components?.core?.engine?.clearData(Engine.BrowsingData.select(Engine.BrowsingData.COOKIES))
                }
                if (checkbox_sessions != null && checkbox_sessions!!.isChecked) {
                    context?.components?.core?.engine?.clearData(Engine.BrowsingData.select(Engine.BrowsingData.AUTH_SESSIONS))
                }
                if (checkbox_permissions != null && checkbox_permissions!!.isChecked) {
                    context?.components?.core?.engine?.clearData(Engine.BrowsingData.select(Engine.BrowsingData.PERMISSIONS))
                }
            }
            if (checkbox_history != null && checkbox_history!!.isChecked) {
                MainScope().launch {
                    context?.components?.core?.historyStorage?.deleteEverything()
                }
            }

            Toast.makeText(context, getString(R.string.cleardata_done), Toast.LENGTH_LONG).show()
            this.onBackPressed()
        }
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment_container, PrivacySettingsFragment(), "SETTINGS_PRIVACY_FRAGMENT")
                .addToBackStack(null)
                .commit()
        return true
    }
}