/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_on_settings.*
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.addons.Addon
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * An activity to show the settings of an add-on.
 */
class AddonSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_on_settings)

        val addon = intent.getParcelableExtra<Addon>("add_on")
        title = addon?.translatableName?.translate()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.addonSettingsContainer, AddonSettingsFragment.create(addon))
            .commit()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            else -> super.onCreateView(parent, name, context, attrs)
        }

    /**
     * A fragment to show the settings of an add-on with [EngineView].
     */
    class AddonSettingsFragment : Fragment() {
        private var addon: Addon? = null
        private lateinit var engineSession: EngineSession

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // addon = requireNotNull(arguments?.getParcelable("add_on"))

            addon = arguments?.getParcelable("add_on")
            engineSession = requireContext().components.core.engine.createSession()

            return inflater.inflate(R.layout.fragment_add_on_settings, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            addonSettingsEngineView.render(engineSession)
            val s = addon?.installedState!!.optionsPageUrl
            if (s != null) engineSession.loadUrl(s)
        }

        override fun onDestroyView() {
            engineSession.close()
            super.onDestroyView()
        }

        companion object {
            /**
             * Create an [AddonSettingsFragment] with add_on as a required parameter.
             */
            fun create(addon: Addon?) = AddonSettingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("add_on", addon)
                }
            }
        }
    }
}
