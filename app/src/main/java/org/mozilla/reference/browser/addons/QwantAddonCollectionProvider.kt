package org.mozilla.reference.browser.addons

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.fetch.Request
import mozilla.components.concept.fetch.isSuccess
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonsProvider
import mozilla.components.support.ktx.kotlin.sanitizeURL
import java.io.IOException

class QwantAddonCollectionProvider(private val client: Client) : AddonsProvider {
    /* override suspend fun getAvailableAddons(allowCache: Boolean, readTimeoutInSeconds: Long?): List<Addon> {
        return listOf(Addon(
            id = "{d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}",
            authors = listOf(Addon.Author(id="adblock_plus", name = "Adblock plus creator name", username = "Adblock plus creator username", url = "https://addons.mozilla.org/fr/firefox/user/13462625/")),
            categories = listOf("security-privacy"),
            downloadId = "3694628",
            downloadUrl = "https://addons.mozilla.org/firefox/downloads/file/3694628/adblock_plus-3.10.1-an+fx.xpi",
            version = "3.10.1",
            permissions =  listOf("tabs", "<all_urls>", "contextMenus", "webRequest", "webRequestBlocking", "webNavigation", "storage", "unlimitedStorage", "notifications"),
            iconUrl = "https://addons.cdn.mozilla.net/user-media/addon_icons/1/1865-64.png?modified=mcrushed",
            siteUrl = "https://adblockplus.org/",
            rating = Addon.Rating(4.6f, 13530),
            // createdAt = "",
            updatedAt = "2020-12-15T00:00:00Z",
            // installedState = , // App handled ??
            defaultLocale = "en_US",
            translatableName = mapOf("en_US" to "Adblock Plus"),
            translatableSummary = mapOf("en_US" to "One of the most popular free ad blockers for Firefox. Block annoying ads on sites like Facebook, YouTube and all other websites.\n" +
                    "Adblock Plus blocks all annoying ads, and supports websites by not blocking unobtrusive ads by default (configurable)."),
            translatableDescription = mapOf("en_US" to "Get the <b>free ad blocker for Firefox</b>. With almost 500 million downloads to date!<br/><br/>" +
                    "✓ Block annoying ads and popups<br/>" +
                    "✓ Block video ads on sites like YouTube<br/>" +
                    "✓ Speed-up loading time on pages<br/>" +
                    "✓ Reduce risk of \"malvertising\" infections<br/>" +
                    "✓ Protect your privacy by stopping trackers from following your online activity<br/>" +
                    "✓ Block social media icons tracking<br/><br/>" +
                    "The ad blocker's additional features enable you to easily support your favorite websites by whitelisting them, to add or create your own filters, and to block social media icons tracking.<br/><br/>" +
                    "Adblock Plus supports the Acceptable Ads initiative. Acceptable Ads are shown by default, which helps support websites that rely on advertising revenue but choose to only display nonintrusive ads. This can be disabled at any time for users who wish to block all ads. The initiative allows content producers to receive monetization for their work and helps create an environment of fairness and sustainability for user, advertiser, and creator alike. Learn more<br/><br/>" +
                    "By downloading and installing this extension, you agree to our Terms of Use and our Privacy Policy.")
        ))
    } */

    @Throws(IOException::class)
    suspend fun getAddonIconBitmap(addon: Addon): Bitmap? {
        var bitmap: Bitmap? = null
        if (addon.iconUrl != "") {
            client.fetch(
                Request(url = addon.iconUrl.sanitizeURL())
            ).use { response ->
                if (response.isSuccess) {
                    response.body.useStream {
                        bitmap = BitmapFactory.decodeStream(it)
                    }
                }
            }
        }

        return bitmap
    }

    override suspend fun getAvailableAddons(allowCache: Boolean, readTimeoutInSeconds: Long?, language: String?): List<Addon> {
        return listOf(Addon(
                id = "{d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}",
                authors = listOf(Addon.Author(id="adblock_plus", name = "Adblock plus creator name", username = "Adblock plus creator username", url = "https://addons.mozilla.org/fr/firefox/user/13462625/")),
                categories = listOf("security-privacy"),
                downloadId = "3694628",
                downloadUrl = "https://addons.mozilla.org/firefox/downloads/file/3694628/adblock_plus-3.10.1-an+fx.xpi",
                version = "3.10.1",
                permissions =  listOf("tabs", "<all_urls>", "contextMenus", "webRequest", "webRequestBlocking", "webNavigation", "storage", "unlimitedStorage", "notifications"),
                iconUrl = "https://addons.cdn.mozilla.net/user-media/addon_icons/1/1865-64.png?modified=mcrushed",
                siteUrl = "https://adblockplus.org/",
                rating = Addon.Rating(4.6f, 13530),
                // createdAt = "",
                updatedAt = "2020-12-15T00:00:00Z",
                // installedState = , // App handled ??
                defaultLocale = "en_US",
                translatableName = mapOf("en_US" to "Adblock Plus"),
                translatableSummary = mapOf("en_US" to "One of the most popular free ad blockers for Firefox. Block annoying ads on sites like Facebook, YouTube and all other websites.\n" +
                        "Adblock Plus blocks all annoying ads, and supports websites by not blocking unobtrusive ads by default (configurable)."),
                translatableDescription = mapOf("en_US" to "Get the <b>free ad blocker for Firefox</b>. With almost 500 million downloads to date!<br/><br/>" +
                        "✓ Block annoying ads and popups<br/>" +
                        "✓ Block video ads on sites like YouTube<br/>" +
                        "✓ Speed-up loading time on pages<br/>" +
                        "✓ Reduce risk of \"malvertising\" infections<br/>" +
                        "✓ Protect your privacy by stopping trackers from following your online activity<br/>" +
                        "✓ Block social media icons tracking<br/><br/>" +
                        "The ad blocker's additional features enable you to easily support your favorite websites by whitelisting them, to add or create your own filters, and to block social media icons tracking.<br/><br/>" +
                        "Adblock Plus supports the Acceptable Ads initiative. Acceptable Ads are shown by default, which helps support websites that rely on advertising revenue but choose to only display nonintrusive ads. This can be disabled at any time for users who wish to block all ads. The initiative allows content producers to receive monetization for their work and helps create an environment of fairness and sustainability for user, advertiser, and creator alike. Learn more<br/><br/>" +
                        "By downloading and installing this extension, you agree to our Terms of Use and our Privacy Policy.")
        ))
    }
}