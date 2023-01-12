package com.qwant.android.compose.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.mozilla.reference.browser.R

@Composable
fun DeleteTabsButton(
    private: Boolean,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogOpened by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(R.drawable.ic_trash),
        contentDescription = "privacy tabs",
        colorFilter = ColorFilter.tint(colorResource(id = R.color.qwant_text)),
        modifier = modifier.clickable { dialogOpened = true }
    )

    if (dialogOpened) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = { Text(if (private) stringResource(R.string.menu_action_close_tabs_private) else stringResource(
                R.string.menu_action_close_tabs)
            ) },
            text = { Text(if (private) stringResource(R.string.close_tabs_confirm_private) else stringResource(
                R.string.close_tabs_confirm)
            ) },
            onDismissRequest = { dialogOpened = false },
            dismissButton = {
                TextButton(onClick = { dialogOpened = false }) {
                    Text(stringResource(android.R.string.no))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogOpened = false
                    onDeleteConfirmed()
                }) {
                    Text(stringResource(android.R.string.yes))
                }
            }
        )
    }
}