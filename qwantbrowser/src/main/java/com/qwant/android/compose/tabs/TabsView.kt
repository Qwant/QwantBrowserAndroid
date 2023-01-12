package com.qwant.android.compose.tabs

import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.R

@Composable
fun TabsView(
    store: BrowserStore,
    tabsUseCases: TabsUseCases,
    thumbnailStorage: ThumbnailStorage,
    homepageUrl: String,
    // updatePrivacyTheme: (isPrivate: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var private by remember { mutableStateOf(store.state.selectedTab?.content?.private ?: false) }
    val onPrivateChange = { isPrivate: Boolean ->
        // updatePrivacyTheme(isPrivate)
        private = isPrivate
    }

    val colorBlue = colorResource(R.color.qwant_blue_v2)
    val colorPurple = colorResource(R.color.qwant_purple_base)
    val privacyColor = remember { Animatable(if (private) colorPurple else colorBlue) }
    LaunchedEffect(private) {
        privacyColor.animateTo(if (private) colorPurple else colorBlue, animationSpec = tween(300))
    }

    val deleteSuccessToast = if (private) stringResource(R.string.close_tabs_done_private) else stringResource(R.string.close_tabs_done)

    Scaffold(
        backgroundColor = colorResource(R.color.qwant_background),
        topBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
            ) {
                Image( /* Back Button */
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.qwant_text)),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .padding(12.dp)
                        .clickable { onClose() }
                )

                TabPrivacySwitch(
                    store = store,
                    private = private,
                    onPrivateChange = onPrivateChange,
                    privacyColor = privacyColor.value,
                    modifier = Modifier.align(Alignment.Center)
                )

                DeleteTabsButton(
                    private = private,
                    onDeleteConfirmed = {
                        if (private) {
                            tabsUseCases.removePrivateTabs.invoke()
                            onPrivateChange(false)
                        } else {
                            tabsUseCases.removeNormalTabs.invoke()
                            tabsUseCases.addTab.invoke(homepageUrl, selectTab = true)
                            onClose()
                        }
                        Toast.makeText(context, deleteSuccessToast, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                        .padding(12.dp)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            TabList(
                store = store,
                private = private,
                thumbnailStorage = thumbnailStorage,
                modifier = Modifier.fillMaxHeight(),
                onTabSelected = { tab ->
                    tabsUseCases.selectTab.invoke(tab.id)
                    onClose()
                },
                onTabDeleted = { tab -> tabsUseCases.removeTab.invoke(tab.id) }
            )

            Box(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
            ) {
                NewTabButton(
                    private = private,
                    privacyColor = privacyColor.value,
                    onClick = {
                        tabsUseCases.addTab.invoke(homepageUrl, selectTab = true, private = private)
                        onClose()
                    },
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}