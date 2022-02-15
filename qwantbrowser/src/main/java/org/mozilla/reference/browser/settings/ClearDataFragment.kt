package org.mozilla.reference.browser.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.Engine
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.QwantUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class ClearDataFragment(
        private val immediate: Boolean,
        private var settingsContainer: SettingsContainerFragment? = null
): Fragment(), UserInteractionHandler {
    private var checkbox_allsites: AppCompatCheckBox? = null
    private var checkbox_cache: AppCompatCheckBox? = null
    private var checkbox_cookies: AppCompatCheckBox? = null
    private var checkbox_sessions: AppCompatCheckBox? = null
    private var checkbox_permissions: AppCompatCheckBox? = null

    private var checkbox_history: AppCompatCheckBox? = null
    private var checkbox_tabs: AppCompatCheckBox? = null
    private var checkbox_tabs_private: AppCompatCheckBox? = null

    private var toggle_layout: LinearLayout? = null
    private var toggle: SwitchCompat? = null
    private var toggle_text: TextView? = null
    private var toggle_summary: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_clear_data, container, false)
    }

    fun setSettingsContainerFragment(settingsContainer: SettingsContainerFragment) {
        this.settingsContainer = settingsContainer
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkbox_allsites = view.findViewById(R.id.cleardata_check_allsites)
        checkbox_cache = view.findViewById(R.id.cleardata_check_cache)
        checkbox_cookies = view.findViewById(R.id.cleardata_check_cookies)
        checkbox_sessions = view.findViewById(R.id.cleardata_check_sessions)
        checkbox_permissions = view.findViewById(R.id.cleardata_check_permissions)

        checkbox_history = view.findViewById(R.id.cleardata_check_history)
        checkbox_tabs = view.findViewById(R.id.cleardata_check_tabs)
        checkbox_tabs_private = view.findViewById(R.id.cleardata_check_tabs_private)

        toggle_layout = view.findViewById(R.id.clear_data_enable_toggle_layout)
        toggle = view.findViewById(R.id.clear_data_enable_toggle)
        toggle_text = view.findViewById(R.id.clear_data_enable_toggle_text)
        toggle_summary = view.findViewById(R.id.clear_data_enable_toggle_summary)

        if (immediate) {
            settingsContainer?.setTitle(getString(R.string.settings_privacy_clear_data))
            toggle_layout?.visibility = View.GONE
            view.findViewById<TextView>(R.id.cleardata_button).setOnClickListener {
                QwantUtils.clearData(requireContext(),
                    history = checkbox_history?.isChecked ?: false,
                    tabs = checkbox_tabs?.isChecked ?: false,
                    privateTabs = checkbox_tabs_private?.isChecked ?: false,
                    browsingData = getBrowsingDataValue(),
                    success = {
                        Toast.makeText(context, getString(R.string.cleardata_done), Toast.LENGTH_LONG).show()
                        this.onBackPressed()
                    },
                    error = {
                        Toast.makeText(context, R.string.cleardata_failed_engine, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        else {
            settingsContainer?.setTitle(getString(R.string.settings_privacy_clear_data_on_close))
            view.findViewById<TextView>(R.id.cleardata_button).visibility = View.GONE
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            checkbox_tabs_private?.isChecked = true
            checkbox_tabs_private?.isEnabled = false

            val isEnabled = prefs.getBoolean(getString(R.string.pref_key_privacy_cleardata_on_close), false)
            toggle?.setOnCheckedChangeListener { _, isChecked ->
                // Prefs are saved when quitting fragment
                this.setToggleText(isChecked)
                if (isChecked) this.restoreCheckboxesFromPrefs(prefs)
                else {
                    // Save checkboxes prefs also on disable, before resetting checkboxes
                    with(prefs.edit()) {
                        putBoolean(getString(R.string.pref_key_cleardata_content_history), checkbox_history?.isChecked ?: false)
                        putBoolean(getString(R.string.pref_key_cleardata_content_tabs), checkbox_tabs?.isChecked ?: false)
                        putBoolean(getString(R.string.pref_key_cleardata_content_tabs_private), checkbox_tabs_private?.isChecked ?: false)
                        putInt(getString(R.string.pref_key_cleardata_content_browsingdata), getBrowsingDataValue().types)
                        apply()
                    }
                    this.clearAndDisableCheckboxes()
                }
            }
            toggle?.isChecked = isEnabled
            this.setToggleText(isEnabled)
            if (isEnabled) this.restoreCheckboxesFromPrefs(prefs)
            else this.clearAndDisableCheckboxes()
        }

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
    }

    private fun setToggleText(isChecked: Boolean) {
        toggle_text?.text = getString(if (isChecked) R.string.enabled else R.string.disabled)
        toggle_summary?.visibility = if (isChecked) View.VISIBLE else View.GONE
    }

    private fun restoreCheckboxesFromPrefs(prefs: SharedPreferences) {
        val browsingDataInt = prefs.getInt(getString(R.string.pref_key_cleardata_content_browsingdata), 0)
        val browsingData = Engine.BrowsingData.select(browsingDataInt)
        checkbox_allsites?.isChecked = browsingData.contains(Engine.BrowsingData.ALL_SITE_DATA) && browsingData.contains(Engine.BrowsingData.AUTH_SESSIONS)
        checkbox_cache?.isChecked = browsingData.contains(Engine.BrowsingData.ALL_CACHES)
        checkbox_cookies?.isChecked = browsingData.contains(Engine.BrowsingData.COOKIES)
        checkbox_sessions?.isChecked = browsingData.contains(Engine.BrowsingData.AUTH_SESSIONS)
        checkbox_permissions?.isChecked = browsingData.contains(Engine.BrowsingData.PERMISSIONS)

        checkbox_history?.isChecked = prefs.getBoolean(getString(R.string.pref_key_cleardata_content_history), false)
        checkbox_tabs?.isChecked = prefs.getBoolean(getString(R.string.pref_key_cleardata_content_tabs), false)


        checkbox_allsites?.isEnabled = true
        checkbox_cache?.isEnabled = true
        checkbox_cookies?.isEnabled = true
        checkbox_sessions?.isEnabled = true
        checkbox_permissions?.isEnabled = true
        checkbox_history?.isEnabled = true
        checkbox_tabs?.isEnabled = true
    }

    private fun clearAndDisableCheckboxes() {
        checkbox_allsites?.isChecked = false
        checkbox_cache?.isChecked = false
        checkbox_cookies?.isChecked = false
        checkbox_sessions?.isChecked = false
        checkbox_permissions?.isChecked = false
        checkbox_history?.isChecked = false
        checkbox_tabs?.isChecked = false

        checkbox_allsites?.isEnabled = false
        checkbox_cache?.isEnabled = false
        checkbox_cookies?.isEnabled = false
        checkbox_sessions?.isEnabled = false
        checkbox_permissions?.isEnabled = false
        checkbox_history?.isEnabled = false
        checkbox_tabs?.isEnabled = false
    }

    private fun getBrowsingDataValue() : Engine.BrowsingData {
        val browsingDataArray = arrayListOf<Int>()

        if (checkbox_allsites != null && checkbox_allsites!!.isChecked) {
            return Engine.BrowsingData.select(Engine.BrowsingData.ALL_SITE_DATA, Engine.BrowsingData.AUTH_SESSIONS)
        } else {
            if (checkbox_cache != null && checkbox_cache!!.isChecked) {
                browsingDataArray.add(Engine.BrowsingData.ALL_CACHES)
            }
            if (checkbox_cookies != null && checkbox_cookies!!.isChecked) {
                browsingDataArray.add(Engine.BrowsingData.COOKIES)
            }
            if (checkbox_sessions != null && checkbox_sessions!!.isChecked) {
                browsingDataArray.add(Engine.BrowsingData.AUTH_SESSIONS)
            }
            if (checkbox_permissions != null && checkbox_permissions!!.isChecked) {
                browsingDataArray.add(Engine.BrowsingData.PERMISSIONS)
            }
        }
        return Engine.BrowsingData.select(*browsingDataArray.toIntArray())
    }

    override fun onBackPressed(): Boolean {
        if (!immediate) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            with(prefs.edit()) {
                putBoolean(getString(R.string.pref_key_privacy_cleardata_on_close), toggle?.isChecked ?: false)
                if (toggle?.isChecked == true) { // Save only if checked to preserve selection on toggle
                    putBoolean(getString(R.string.pref_key_cleardata_content_history), checkbox_history?.isChecked ?: false)
                    putBoolean(getString(R.string.pref_key_cleardata_content_tabs), checkbox_tabs?.isChecked ?: false)
                    putBoolean(getString(R.string.pref_key_cleardata_content_tabs_private), checkbox_tabs_private?.isChecked ?: false)
                    putInt(getString(R.string.pref_key_cleardata_content_browsingdata), getBrowsingDataValue().types)
                }
                apply()
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, PrivacySettingsFragment(), "SETTINGS_PRIVACY_FRAGMENT")
            .addToBackStack(null)
            .commit()
        return true
    }
}