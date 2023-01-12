package com.qwant.android.compose.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import org.mozilla.reference.browser.R

@Composable
fun TabRow(
    tab: TabSessionState,
    thumbnailStorage: ThumbnailStorage,
    onSelected: (tab: TabSessionState) -> Unit,
    onDeleted: (tab: TabSessionState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(start = 20.dp, bottom = 16.dp)
        .clickable { onSelected(tab) }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 3.dp,
            backgroundColor = Color.White,
            modifier = Modifier
                .width(90.dp)
                .height(70.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icons_business_global_line),
                colorFilter = ColorFilter.tint(colorResource(id = R.color.qwant_grey_semi_light)),
                contentDescription = "Thumbnail placeholder",
                contentScale = ContentScale.None
            )
            TabThumbnail(tab.id, 90.dp, thumbnailStorage)
        }

        Box(modifier = Modifier
            .weight(1.0f)
            .height(70.dp)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                tab.content.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                color = colorResource(id = R.color.qwant_text),
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                tab.content.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = colorResource(id = R.color.qwant_tabs_url),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        Box(modifier = Modifier
            .width(44.dp)
            .height(70.dp)
            .padding(end = 12.dp)
            .clickable { onDeleted(tab) }
        ) {
            Image(
                painter = painterResource(id = R.drawable.icons_system_close_line),
                colorFilter = ColorFilter.tint(colorResource(id = R.color.qwant_text)),
                contentDescription = "Delete",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}