package com.example.android.minutelauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp

@Composable
fun AppCard(
  appTitle: String,
  appUsage: Long,
  editState: Boolean = false,
  isDragged: Boolean = false,
  onClick: () -> Unit
) {
  val tonalElevation by animateDpAsState(
    targetValue =
    if (editState) {
      if (isDragged) 10.dp else 2.dp
    } else 0.dp,
    label = ""
  )
  val unboundedVerticalPadding by animateDpAsState(
    targetValue = if (editState) 16.dp else 2.dp,
    label = "",
    animationSpec = tween(600)
  )
  val verticalPadding = max(0.dp, unboundedVerticalPadding)

  Surface(
    onClick = { onClick() },
    tonalElevation = tonalElevation,
    shape = MaterialTheme.shapes.large,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp, horizontal = verticalPadding)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(vertical = verticalPadding)
    ) {
      Text(
        text = appTitle,
        fontSize = 23.sp,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
      )
      AnimatedVisibility(
        visible = !editState,
        enter = fadeIn(tween(500, 600)),
        exit = fadeOut(tween(150))
      ) {
        Text(appUsage.toTimeUsed(), color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}
