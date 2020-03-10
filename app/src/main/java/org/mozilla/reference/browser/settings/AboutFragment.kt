/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_about.*
import mozilla.components.Build
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VERSION
import org.mozilla.reference.browser.R

class AboutFragment(
        val settingsContainer: SettingsContainerFragment
) : Fragment(), UserInteractionHandler {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsContainer.setTitle(resources.getString(R.string.settings_about))
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName = requireContext().resources.getString(R.string.app_name)
        (activity as AppCompatActivity).title = getString(R.string.preferences_about_page)

        val aboutText = try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val geckoVersion = PackageInfoCompat.getLongVersionCode(packageInfo).toString() + " - Gecko: " +
                MOZ_APP_VERSION
            String.format(
                "%s (Build %s)",
                packageInfo.versionName,
                geckoVersion
            )
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }

        about_content.text = HtmlCompat.fromHtml(
                resources.getString(R.string.about_content, appName),
                FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM
        )
        version_info.text = aboutText

        version_info.setOnTouchListener { _, _ ->
            val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipBoard.primaryClip = ClipData.newPlainText(aboutText, aboutText)

            Toast.makeText(requireContext(), getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, AboutMenuFragment(settingsContainer), "SETTINGS_ABOUTMENU_FRAGMENT")
            .addToBackStack(null)
            .commit()
        return true
    }
}
