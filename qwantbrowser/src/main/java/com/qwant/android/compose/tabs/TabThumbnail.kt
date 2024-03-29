package com.qwant.android.compose.tabs

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.support.ktx.android.util.dpToPx

@Composable
fun TabThumbnail(
    tabId: String,
    size: Dp,
    thumbnailStorage: ThumbnailStorage,
    modifier: Modifier = Modifier
) {
    val pixelSize = with(LocalDensity.current) { size.roundToPx() }
    var loadedImage: Bitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(tabId) {
        loadedImage = thumbnailStorage.loadThumbnail(ImageLoadRequest(id = tabId, pixelSize)).await()
    }

    if (loadedImage != null) {
        Image(
            bitmap = loadedImage!!.asImageBitmap(),
            contentDescription = "Tab Thumbnail",
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = modifier.fillMaxWidth()
        )
    }
}