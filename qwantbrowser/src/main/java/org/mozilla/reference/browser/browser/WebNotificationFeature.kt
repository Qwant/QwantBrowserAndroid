package org.mozilla.reference.browser.browser

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.reference.browser.browser.icons.BrowserIcons
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.webnotifications.WebNotification
import mozilla.components.concept.engine.webnotifications.WebNotificationDelegate
import mozilla.components.support.base.ids.SharedIdsHelper
import mozilla.components.support.base.log.logger.Logger
import java.lang.UnsupportedOperationException


import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.core.net.toUri
import mozilla.components.browser.icons.Icon.Source
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.icons.IconRequest.Size
import org.mozilla.reference.browser.R

private const val NOTIFICATION_CHANNEL_ID = "mozac.feature.webnotifications.generic.channel"
private const val NOTIFICATION_ID = 1
private const val PENDING_INTENT_TAG = "mozac.feature.webnotifications.generic.pendingintent"

/**
 * Feature implementation for configuring and displaying web notifications to the user.
 *
 * Initialize this feature globally once on app start
 * ```Kotlin
 * WebNotificationFeature(
 *     applicationContext, engine, icons, R.mipmap.ic_launcher, BrowserActivity::class.java
 * )
 * ```
 *
 * @param context The application Context.
 * @param engine The browser engine.
 * @param browserIcons The entry point for loading the large icon for the notification.
 * @param smallIcon The small icon for the notification.
 * @param activityClass The Activity that the notification will launch if user taps on it
 */

internal class NativeNotificationBridge(
        private val icons: BrowserIcons,
        @DrawableRes private val smallIcon: Int
) {
    companion object {
        private const val EXTRA_ON_CLICK = "mozac.feature.webnotifications.generic.onclick"
    }

    /**
     * Create a system [Notification] from this [WebNotification].
     */
    @Suppress("LongParameterList")
    suspend fun convertToAndroidNotification(
            notification: WebNotification,
            context: Context,
            channelId: String,
            activityClass: Class<out Activity>?,
            requestId: Int
    ): Notification {
        val builder = if (SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("Deprecation")
            Notification.Builder(context)
        }

        with(notification) {
            activityClass?.let {
                val intent = Intent(context, activityClass).apply {
                    putExtra(EXTRA_ON_CLICK, tag)
                }

                PendingIntent.getActivity(context, requestId, intent, PendingIntent.FLAG_IMMUTABLE).apply {
                    builder.setContentIntent(this)
                }
            }

            builder.setSmallIcon(smallIcon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setShowWhen(true)
                    .setWhen(timestamp)
                    .setAutoCancel(true)

            loadIcon(iconUrl?.toUri(), Size.DEFAULT)?.let { iconBitmap ->
                builder.setLargeIcon(iconBitmap)
            }
        }

        return builder.build()
    }

    /**
     * Load an icon for a notification.
     */
    private suspend fun loadIcon(url: Uri?, size: Size): Bitmap? {
        url ?: return null
        val icon = icons.loadIconAsync(IconRequest(
                url = url.toString(),
                size = size,
                resources = listOf(IconRequest.Resource(
                        url = url.toString(),
                        type = IconRequest.Resource.Type.MANIFEST_ICON
                ))
        )).await()

        return if (icon.source == Source.GENERATOR) null else icon.bitmap
    }
}

class WebNotificationFeature(
        private val context: Context,
        engine: Engine,
        browserIcons: BrowserIcons,
        @DrawableRes private val smallIcon: Int,
        private val activityClass: Class<out Activity>?
) : WebNotificationDelegate {
    private val logger = Logger("WebNotificationFeature")
    private val notificationManager = context.getSystemService<NotificationManager>()
    private val nativeNotificationBridge = NativeNotificationBridge(browserIcons, smallIcon)

    init {
        try {
            engine.registerWebNotificationDelegate(this)
        } catch (e: UnsupportedOperationException) {
            logger.error("failed to register for web notification delegate", e)
        }
    }

    override fun onShowNotification(webNotification: WebNotification) {
        ensureNotificationGroupAndChannelExists()
        notificationManager?.cancel(webNotification.tag, NOTIFICATION_ID)

        GlobalScope.launch(Dispatchers.IO) {
            val notification = nativeNotificationBridge.convertToAndroidNotification(
                    webNotification, context, NOTIFICATION_CHANNEL_ID, activityClass,
                    SharedIdsHelper.getNextIdForTag(context, PENDING_INTENT_TAG))
            notificationManager?.notify(webNotification.tag, NOTIFICATION_ID, notification)
        }
    }

    override fun onCloseNotification(webNotification: WebNotification) {
        notificationManager?.cancel(webNotification.tag, NOTIFICATION_ID)
    }

    private fun ensureNotificationGroupAndChannelExists() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.mozac_feature_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            )
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE

            notificationManager?.createNotificationChannel(channel)
        }
    }
}
