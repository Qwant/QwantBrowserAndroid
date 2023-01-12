package com.qwant.android.compose.tabs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.mozilla.reference.browser.R


@Composable
fun NewTabButton(
    private: Boolean,
    privacyColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = privacyColor),
        shape = RoundedCornerShape(50),
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(
            painter = painterResource(if (private) R.drawable.icons_custom_privacy_fill_small else R.drawable.icons_system_add_circle_line),
            tint = Color.White,
            contentDescription = "Add tab",
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (private) stringResource(id = R.string.menu_action_add_tab_private) else stringResource(id = R.string.menu_action_add_tab),
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}