package com.example.android.minutelauncher

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
  onLongPress: () -> Unit = {},
  onClick: () -> Unit
) {
  val haptic = LocalHapticFeedback.current

  Column(
    modifier = Modifier
      .padding(2.dp)
      .combinedClickable(onLongClick = {
        Log.d("APP_CARD", "long press")
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongPress()
      }) {
        Log.d("APP_CARD", "click")
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
