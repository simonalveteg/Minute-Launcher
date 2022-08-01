package com.example.android.minutelauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    appTitle: String,
    appUsage: Long,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val selected = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current


    Box {
        DropdownMenu(expanded = selected.value, onDismissRequest = { selected.value = false }) {
            DropdownMenuItem(text = { Text("Favorite") }, onClick = { onToggleFavorite() })
            DropdownMenuItem(text = { Text("Uninstall") }, onClick = { /*TODO*/ })
            DropdownMenuItem(text = { Text("Hide") }, onClick = { /*TODO*/ })
        }
        Column(
            modifier = Modifier
                .padding(2.dp)
                .combinedClickable(onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selected.value = !selected.value
                }) {
                    onClick()
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = appTitle,
                fontSize = 23.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Text(appUsage.toTimeUsed(), color = MaterialTheme.colorScheme.primary)
        }
    }
}