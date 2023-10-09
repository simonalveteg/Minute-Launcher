package com.example.android.minutelauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    if (editState) 1.dp else 0.dp,
    label = ""
  )
  val surfaceColor by animateColorAsState(
    targetValue = if (editState) {
      if (isDragged) MaterialTheme.colorScheme.surface.copy(alpha = 0.99f)
      else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    } else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
    label = "",
    animationSpec = tween(700)
  )
  val verticalPadding by animateDpAsState(
    targetValue = if (editState) 16.dp else 2.dp,
    label = "",
    animationSpec = tween(300)
  )

  Surface(
    onClick = { onClick() },
    tonalElevation = tonalElevation,
    shape = MaterialTheme.shapes.large,
    color = surfaceColor,
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
