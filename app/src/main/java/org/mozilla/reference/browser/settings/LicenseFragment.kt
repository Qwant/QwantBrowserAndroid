package org.mozilla.reference.browser.settings

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_license.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R

class LicenseFragment: Fragment(), UserInteractionHandler {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_license, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            license_content.text = Html.fromHtml(resources.getString(R.string.mpl_license_content), Html.FROM_HTML_MODE_COMPACT)
        } else {
            license_content.text = Html.fromHtml(resources.getString(R.string.mpl_license_content))
        }
        license_content.movementMethod = ScrollingMovementMethod()
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, AboutMenuFragment(), "SETTINGS_ABOUTMENU_FRAGMENT")
            .addToBackStack(null)
            .commit()
        return true
    }
}