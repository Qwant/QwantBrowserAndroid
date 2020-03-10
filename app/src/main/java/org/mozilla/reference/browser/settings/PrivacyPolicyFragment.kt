package org.mozilla.reference.browser.settings

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_privacy_policy.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R

class PrivacyPolicyFragment(
    private val settingsContainer: SettingsContainerFragment
) : Fragment(), UserInteractionHandler {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        settingsContainer.setTitle(resources.getString(R.string.settings_privacy_policy))
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            privacy_policy_content.text = Html.fromHtml(resources.getString(R.string.privacy_policy_content), Html.FROM_HTML_MODE_COMPACT)
        } else {
            privacy_policy_content.text = Html.fromHtml(resources.getString(R.string.privacy_policy_content))
        }
        privacy_policy_content.movementMethod = ScrollingMovementMethod()
    }
    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, AboutMenuFragment(settingsContainer), "SETTINGS_ABOUTMENU_FRAGMENT")
            .addToBackStack(null)
            .commit()
        return true
    }
}