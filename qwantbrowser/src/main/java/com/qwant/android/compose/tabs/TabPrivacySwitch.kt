package com.qwant.android.compose.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.qwant.android.compose.utils.animateAlignmentAsState
import mozilla.components.browser.state.store.BrowserStore
import org.mozilla.reference.browser.R

@Composable
fun TabPrivacySwitch(
    store: BrowserStore,
    private: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    privacyColor: Color,
    modifier: Modifier = Modifier,
) {
    val selectorAlignment by animateAlignmentAsState(if (private) Alignment.CenterEnd else Alignment.CenterStart)

    Box(modifier = modifier
        .width(140.dp)
        .height(40.dp)
        .background(
            colorResource(R.color.qwant_tabs_switch_button_background),
            shape = RoundedCornerShape(50)
        )
    ) {
        Box(
            Modifier
                .width(70.dp)
                .height(40.dp)
                .background(privacyColor, shape = RoundedCornerShape(50))
                .align(selectorAlignment)
        ) {}

        Row(verticalAlignment = Alignment.CenterVertically) {
            TabCounterButton(
                store = store,
                onClicked = { onPrivateChange(false) },
                modifier = Modifier
                    .width(70.dp)
                    .height(40.dp)
                    .padding(8.dp),
                tabsFilter = { tabSessionState -> !tabSessionState.content.private }

            )
            Image(
                painter = painterResource(R.drawable.icons_custom_privacy_fill),
                contentScale = ContentScale.Fit,
                contentDescription = "privacy tabs",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .width(70.dp)
                    .height(40.dp)
                    .padding(8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onPrivateChange(true)
                    }
            )
        }
    }
}