package com.example.android.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.minutelauncher.utilities.toTimeUsed

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
  val topPadding by animateDpAsState(
    targetValue = if (editState) 16.dp else 2.dp,
    label = "",
    animationSpec = if (editState) tween(400, 200) else tween(300)
  )
  val usageAlpha by animateFloatAsState(
    targetValue = if (editState) 0f else 1f,
    label = "",
    animationSpec = if (editState) tween(150, 300) else tween(400, 600)
  )
  val textHeight = with(LocalDensity.current) {
    LocalTextStyle.current.lineHeight.toDp()
  }
  val usageHeight by animateDpAsState(
    targetValue = if (editState) 0.dp else textHeight,
    animationSpec = if (editState) tween(150, 100) else tween(300, 300),
    label = ""
  )

  Surface(
    onClick = { onClick() },
    tonalElevation = tonalElevation,
    shape = MaterialTheme.shapes.large,
    color = surfaceColor,
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize()
      .padding(vertical = 2.dp, horizontal = topPadding)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(vertical = topPadding)
    ) {
      Text(
        text = appTitle,
        fontSize = 23.sp,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
      )
      Text(
        text = appUsage.toTimeUsed(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = usageAlpha),
        modifier = Modifier.height(usageHeight)
      )
    }
  }
}
