package org.mozilla.reference.browser.downloads

import android.annotation.SuppressLint
import android.app.Dialog
import mozilla.components.feature.downloads.DownloadDialogFragment
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.downloads.SimpleDownloadDialogFragment

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.downloads.DownloadDialogFragment.Companion.FRAGMENT_TAG
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.AndroidDownloadManager
// import org.mozilla.reference.browser.downloads.AndroidDownloadManager

import mozilla.components.feature.downloads.manager.DownloadManager
// import mozilla.components.feature.downloads.manager.noop
import mozilla.components.feature.downloads.manager.onDownloadStopped
import mozilla.components.feature.downloads.ui.DownloaderApp
// import mozilla.components.feature.downloads.ui.DownloadAppChooserDialog
// import mozilla.components.feature.downloads.ui.DownloaderAppAdapter
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.ktx.android.content.appName
import mozilla.components.support.ktx.android.content.isPermissionGranted
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.support.utils.Browsers
import org.mozilla.reference.browser.R
import java.util.ArrayList

internal val noop: onDownloadStopped = { _, _, _ -> }

/**
 * A dialog where an user can select with which app a download must be performed.
 */
internal class DownloadAppChooserDialog : AppCompatDialogFragment() {
    private val safeArguments get() = requireNotNull(arguments)
    internal val appsList: ArrayList<DownloaderApp>
        get() =
            safeArguments.getParcelableArrayList<DownloaderApp>(KEY_APP_LIST) ?: arrayListOf()

    internal val dialogGravity: Int get() =
        safeArguments.getInt(KEY_DIALOG_GRAVITY, DEFAULT_VALUE)
    internal val dialogShouldWidthMatchParent: Boolean get() =
        safeArguments.getBoolean(KEY_DIALOG_WIDTH_MATCH_PARENT)

    /**
     * Indicates the user has selected an application to perform the download
     */
    internal var onAppSelected: ((DownloaderApp) -> Unit) = {}

    internal var onDismiss: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheetDialog = Dialog(requireContext())
        sheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sheetDialog.setCanceledOnTouchOutside(false)

        val rootView = createContainer()
        sheetDialog.setContainerView(rootView)
        sheetDialog.window?.apply {
            if (dialogGravity != DEFAULT_VALUE) {
                setGravity(dialogGravity)
            }

            if (dialogShouldWidthMatchParent) {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // This must be called after addContentView, or it won't fully fill to the edge.
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        return sheetDialog
    }

    @SuppressLint("InflateParams")
    private fun createContainer(): View {
        val rootView = LayoutInflater.from(requireContext()).inflate(
                R.layout.mozac_downloader_chooser_prompt,
                null,
                false
        )

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.apps_list)
        recyclerView.adapter = DownloaderAppAdapter(rootView.context, appsList) { app ->
            onAppSelected(app)
            dismiss()
        }

        rootView.findViewById<AppCompatImageButton>(R.id.close_button).setOnClickListener {
            dismiss()
            onDismiss()
        }

        return rootView
    }

    fun setApps(apps: List<DownloaderApp>) {
        val args = arguments ?: Bundle()
        args.putParcelableArrayList(KEY_APP_LIST, ArrayList(apps))
        arguments = args
    }

    private fun Dialog.setContainerView(rootView: View) {
        if (dialogShouldWidthMatchParent) {
            setContentView(rootView)
        } else {
            addContentView(
                    rootView,
                    LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            )
        }
    }

    companion object {
        /**
         * A builder method for creating a [DownloadAppChooserDialog]
         */
        fun newInstance(
                gravity: Int? = DEFAULT_VALUE,
                dialogShouldWidthMatchParent: Boolean? = false
        ): DownloadAppChooserDialog {
            val fragment = DownloadAppChooserDialog()
            val arguments = fragment.arguments ?: Bundle()

            with(arguments) {
                gravity?.let { putInt(KEY_DIALOG_GRAVITY, it) }
                dialogShouldWidthMatchParent?.let { putBoolean(KEY_DIALOG_WIDTH_MATCH_PARENT, it) }
            }

            fragment.arguments = arguments

            return fragment
        }

        private const val KEY_DIALOG_GRAVITY = "KEY_DIALOG_GRAVITY"
        private const val KEY_DIALOG_WIDTH_MATCH_PARENT = "KEY_DIALOG_WIDTH_MATCH_PARENT"
        private const val DEFAULT_VALUE = Int.MAX_VALUE

        private const val KEY_APP_LIST = "KEY_APP_LIST"
        internal const val FRAGMENT_TAG = "SHOULD_APP_DOWNLOAD_PROMPT_DIALOG"
    }
}

internal class DownloaderAppAdapter(
        context: Context,
        private val apps: List<DownloaderApp>,
        val onAppSelected: ((DownloaderApp) -> Unit)
) : RecyclerView.Adapter<DownloaderAppViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloaderAppViewHolder {
        val view = inflater.inflate(R.layout.mozac_download_app_list_item, parent, false)

        val nameLabel = view.findViewById<TextView>(R.id.app_name)
        val iconImage = view.findViewById<ImageView>(R.id.app_icon)

        return DownloaderAppViewHolder(view, nameLabel, iconImage)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: DownloaderAppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.itemView.context
        with(app) {
            holder.nameLabel.text = name
            holder.iconImage.setImageDrawable(app.resolver.loadIcon(context.packageManager))
            holder.bind(app, onAppSelected)
        }
    }
}

/**
 * View holder for a [DownloaderApp] item.
 */
internal class DownloaderAppViewHolder(
        itemView: View,
        val nameLabel: TextView,
        val iconImage: ImageView
) : RecyclerView.ViewHolder(itemView) {
    fun bind(app: DownloaderApp, onAppSelected: ((DownloaderApp) -> Unit)) {
        itemView.app = app
        itemView.setOnClickListener {
            onAppSelected(it.app)
        }
    }

    internal var View.app: DownloaderApp
        get() = tag as DownloaderApp
        set(value) {
            tag = value
        }
}

/**
 * Feature implementation to provide download functionality for the selected
 * session. The feature will subscribe to the selected session and listen
 * for downloads.
 *
 * @property applicationContext a reference to the application context.
 * @property onNeedToRequestPermissions a callback invoked when permissions
 * need to be requested before a download can be performed. Once the request
 * is completed, [onPermissionsResult] needs to be invoked.
 * @property onDownloadStopped a callback invoked when a download is paused or completed.
 * @property downloadManager a reference to the [DownloadManager] which is
 * responsible for performing the downloads.
 * @property store a reference to the application's [BrowserStore].
 * @property useCases [DownloadsUseCases] instance for consuming processed downloads.
 * @property fragmentManager a reference to a [FragmentManager]. If a fragment
 * manager is provided, a dialog will be shown before every download.
 * @property promptsStyling styling properties for the dialog.
 * @property shouldForwardToThirdParties Indicates if downloads should be forward to third party apps,
 * if there are multiple apps a chooser dialog will shown.
 */
@Suppress("TooManyFunctions", "LongParameterList", "LargeClass")
class DownloadsFeature(
        private val applicationContext: Context,
        private val store: BrowserStore,
        @VisibleForTesting(otherwise = PRIVATE)
        internal val useCases: DownloadsUseCases,
        override var onNeedToRequestPermissions: OnNeedToRequestPermissions = { },
        onDownloadStopped: onDownloadStopped = noop,
        private val downloadManager: DownloadManager = AndroidDownloadManager(applicationContext, store),
        private val tabId: String? = null,
        private val fragmentManager: FragmentManager? = null,
        private val promptsStyling: DownloadsFeature.PromptsStyling? = null,
        private val shouldForwardToThirdParties: () -> Boolean = { false }
) : LifecycleAwareFeature, PermissionsFeature {

    var onDownloadStopped: onDownloadStopped
        get() = downloadManager.onDownloadStopped
        set(value) { downloadManager.onDownloadStopped = value }

    init {
        this.onDownloadStopped = onDownloadStopped
    }

    private var scope: CoroutineScope? = null

    /**
     * Starts observing downloads on the selected session and sends them to the [DownloadManager]
     * to be processed.
     */
    @Suppress("Deprecation")
    override fun start() {
        Log.e("QWANT_BROWSER", "DL start")
        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.findTabOrCustomTabOrSelectedTab(tabId) }
                    .ifChanged { it.content.download }
                    .collect { state ->
                        state.content.download?.let { downloadState ->
                            Log.e("QWANT_BROWSER", "DL start process")
                            processDownload(state, downloadState)
                        }
                    }
        }
    }

    /**
     * Calls the tryAgain function of the corresponding [DownloadManager]
     */
    @Suppress("Unused")
    fun tryAgain(id: Long) {
        Log.e("QWANT_BROWSER", "DL try again")
        downloadManager.tryAgain(id.toString())
    }

    /**
     * Stops observing downloads on the selected session.
     */
    override fun stop() {
        Log.e("QWANT_BROWSER", "DL stop")
        scope?.cancel()
        downloadManager.unregisterListeners()
    }

    /**
     * Notifies the [DownloadManager] that a new download must be processed.
     */
    @VisibleForTesting
    internal fun processDownload(tab: SessionState, download: DownloadState): Boolean {
        Log.e("QWANT_BROWSER", "DL process")
        return if (applicationContext.isPermissionGranted(downloadManager.permissions.asIterable())) {

            Log.e("QWANT_BROWSER", "DL process 1")
            if (shouldForwardToThirdParties()) {
                Log.e("QWANT_BROWSER", "DL process 2")
                val apps = getDownloaderApps(applicationContext, download)

                // We only show the dialog If we have multiple apps that can handle the download.
                if (apps.size > 1) {
                    Log.e("QWANT_BROWSER", "DL process 3")
                    showAppDownloaderDialog(tab, download, apps)
                    return false
                }
                Log.e("QWANT_BROWSER", "DL process 4")
            }

            Log.e("QWANT_BROWSER", "DL process 5")

            if (fragmentManager != null && !download.skipConfirmation) {
                Log.e("QWANT_BROWSER", "DL process 6")
                showDownloadDialog(tab, download)
                false
            } else {
                Log.e("QWANT_BROWSER", "DL process 7")
                useCases.consumeDownload(tab.id, download.id)
                startDownload(download)
            }
        } else {
            onNeedToRequestPermissions(downloadManager.permissions)
            false
        }
    }

    @VisibleForTesting
    internal fun startDownload(download: DownloadState): Boolean {
        Log.e("QWANT_BROWSER", "DL start")
        val id = downloadManager.download(download)
        return if (id != null) {
            Log.e("QWANT_BROWSER", "DL start 1")
            Log.e("QWANT_BROWSER", "DL started status: ${download.status}")
            Log.e("QWANT_BROWSER", "DL started url: ${download.url}")
            Log.e("QWANT_BROWSER", "DL started length: ${download.contentLength}")
            Log.e("QWANT_BROWSER", "DL started download full: $download")
            true
        } else {
            Log.e("QWANT_BROWSER", "DL start 2")
            showDownloadNotSupportedError()
            false
        }
    }

    /**
     * Notifies the feature that the permissions request was completed. It will then
     * either trigger or clear the pending download.
     */
    override fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        Log.e("QWANT_BROWSER", "DL permission result 1")
        if (permissions.isEmpty()) {
            Log.e("QWANT_BROWSER", "DL permission result 2")
            // If we are requesting permissions while a permission prompt is already being displayed
            // then Android seems to call `onPermissionsResult` immediately with an empty permissions
            // list. In this case just ignore it.
            return
        }

        Log.e("QWANT_BROWSER", "DL permission result 3")
        withActiveDownload { (tab, download) ->
            Log.e("QWANT_BROWSER", "DL permission result 4")
            if (applicationContext.isPermissionGranted(downloadManager.permissions.asIterable())) {
                Log.e("QWANT_BROWSER", "DL permission result 5")
                processDownload(tab, download)
            } else {
                Log.e("QWANT_BROWSER", "DL permission result 6")
                useCases.consumeDownload(tab.id, download.id)
            }
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showDownloadNotSupportedError() {
        Log.e("QWANT_BROWSER", "DL not supported 1")
        Toast.makeText(
                applicationContext,
                applicationContext.getString(
                        R.string.mozac_feature_downloads_file_not_supported2,
                        applicationContext.appName),
                Toast.LENGTH_LONG
        ).show()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showDownloadDialog(
            tab: SessionState,
            download: DownloadState,
            dialog: DownloadDialogFragment = getDownloadDialog()
    ) {
        Log.e("QWANT_BROWSER", "DL show dialog 1")
        dialog.setDownload(download)

        dialog.onStartDownload = {
            Log.e("QWANT_BROWSER", "DL dialog onstart")
            startDownload(download)
            useCases.consumeDownload.invoke(tab.id, download.id)
        }

        dialog.onCancelDownload = {
            Log.e("QWANT_BROWSER", "DL dialog oncancel")
            useCases.consumeDownload.invoke(tab.id, download.id)
        }

        if (!isAlreadyADownloadDialog() && fragmentManager != null) {
            Log.e("QWANT_BROWSER", "DL dialog show now")
            dialog.showNow(fragmentManager, FRAGMENT_TAG)
        }
    }

    private fun getDownloadDialog(): DownloadDialogFragment {
        Log.e("QWANT_BROWSER", "DL get dialog")
        return findPreviousDownloadDialogFragment() ?: SimpleDownloadDialogFragment.newInstance(
                promptsStyling = promptsStyling
        )
    }

    @VisibleForTesting
    internal fun showAppDownloaderDialog(
            tab: SessionState,
            download: DownloadState,
            apps: List<DownloaderApp>,
            appChooserDialog: DownloadAppChooserDialog = getAppDownloaderDialog()
    ) {
        Log.e("QWANT_BROWSER", "DL app downloader")
        appChooserDialog.setApps(apps)
        appChooserDialog.onAppSelected = { app ->
            Log.e("QWANT_BROWSER", "DL app downloader 1")
            if (app.packageName == applicationContext.packageName) {
                Log.e("QWANT_BROWSER", "DL app downloader 2")
                startDownload(download)
            } else {
                try {
                    Log.e("QWANT_BROWSER", "DL app downloader 3")
                    applicationContext.startActivity(app.toIntent())
                } catch (error: ActivityNotFoundException) {
                    Log.e("QWANT_BROWSER", "DL app downloader 4")
                    val errorMessage = applicationContext.getString(
                            R.string.mozac_feature_downloads_unable_to_open_third_party_app,
                            app.name
                    )
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
            Log.e("QWANT_BROWSER", "DL app downloader 5")
            useCases.consumeDownload(tab.id, download.id)
        }

        appChooserDialog.onDismiss = {
            Log.e("QWANT_BROWSER", "DL app downloader 6")
            useCases.consumeDownload.invoke(tab.id, download.id)
        }

        if (!isAlreadyAppDownloaderDialog() && fragmentManager != null) {
            Log.e("QWANT_BROWSER", "DL app downloader 7")
            appChooserDialog.showNow(fragmentManager, DownloadAppChooserDialog.FRAGMENT_TAG)
        }
    }

    private fun getAppDownloaderDialog() = findPreviousAppDownloaderDialogFragment()
            ?: DownloadAppChooserDialog.newInstance(
                    promptsStyling?.gravity,
                    promptsStyling?.shouldWidthMatchParent
            )

    @VisibleForTesting
    internal fun isAlreadyAppDownloaderDialog(): Boolean {
        Log.e("QWANT_BROWSER", "DL dialog app already")
        return findPreviousAppDownloaderDialogFragment() != null
    }

    private fun findPreviousAppDownloaderDialogFragment(): DownloadAppChooserDialog? {
        Log.e("QWANT_BROWSER", "DL find prev app")
        return fragmentManager?.findFragmentByTag(DownloadAppChooserDialog.FRAGMENT_TAG) as? DownloadAppChooserDialog
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun isAlreadyADownloadDialog(): Boolean {
        Log.e("QWANT_BROWSER", "DL already dl")
        return findPreviousDownloadDialogFragment() != null
    }

    private fun findPreviousDownloadDialogFragment(): DownloadDialogFragment? {
        Log.e("QWANT_BROWSER", "DL find prev dl")
        return fragmentManager?.findFragmentByTag(FRAGMENT_TAG) as? DownloadDialogFragment
    }

    private fun withActiveDownload(block: (Pair<SessionState, DownloadState>) -> Unit) {
        Log.e("QWANT_BROWSER", "DL with active download")
        val state = store.state.findCustomTabOrSelectedTab(tabId) ?: return
        val download = state.content.download ?: return
        block(Pair(state, download))
    }

    /**
     * Find all apps that can perform a download, including this app.
     */
    @VisibleForTesting
    internal fun getDownloaderApps(context: Context, download: DownloadState): List<DownloaderApp> {
        Log.e("QWANT_BROWSER", "DL get dl apps")
        val packageManager = context.packageManager

        val browsers = Browsers.findResolvers(context, packageManager, includeThisApp = true)
                .associateBy { it.activityInfo.identifier }

        val thisApp = browsers.values
                .firstOrNull { it.activityInfo.packageName == context.packageName }
                ?.toDownloaderApp(context, download)

        val apps = Browsers.findResolvers(
                context,
                packageManager,
                includeThisApp = false,
                url = download.url,
                contentType = download.contentType
        )
        // Remove browsers and returns only the apps that can perform a download plus this app.
        return apps.filter { !browsers.contains(it.activityInfo.identifier) }
                .map { it.toDownloaderApp(context, download) } + listOfNotNull(thisApp)
    }

    private val ActivityInfo.identifier: String get() = packageName + name

    private fun DownloaderApp.toIntent(): Intent {
        Log.e("QWANT_BROWSER", "DL app to intent")
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndTypeAndNormalize(url.toUri(), contentType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setClassName(packageName, activityName)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
    }
}

@VisibleForTesting
internal fun ResolveInfo.toDownloaderApp(context: Context, download: DownloadState): DownloaderApp {
    return DownloaderApp(
            loadLabel(context.packageManager).toString(),
            this,
            activityInfo.packageName,
            activityInfo.name,
            download.url,
            download.contentType
    )
}
