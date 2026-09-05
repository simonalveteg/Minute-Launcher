package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard

@Composable
fun AppList(
  apps: List<AppInfo>,
  onAppClick: (AppInfo) -> Unit,
  listState: LazyListState = rememberLazyListState(),
  offset: Dp = 0.dp,
  searchHeight: Dp = 0.dp
) {
  val fadeHeight = 100.dp
  val canScrollFurther by remember {
    derivedStateOf { listState.canScrollForward }
  }
  val maskStrength by animateFloatAsState(
    targetValue = if (canScrollFurther) 0.85f else 0f,
    animationSpec = tween(durationMillis = 300),
    label = "topFadeMask"
  )

  LazyColumn(
    state = listState,
    verticalArrangement = Arrangement.Bottom,
    reverseLayout = true,
    modifier = Modifier
      .fillMaxSize()
      .offset(y = offset)
      .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
      .drawWithContent {
        drawContent()
        if (maskStrength > 0f) {
          drawRect(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 1f - maskStrength),
                Color.Black
              ),
              startY = 0f,
              endY = fadeHeight.toPx()
            ),
            blendMode = BlendMode.DstIn
          )
        }
      }
  ) {
    item {
      Spacer(
        modifier = Modifier
          .navigationBarsPadding()
          .height(searchHeight + 16.dp)
      )
    }
    items(items = apps) { appInfo ->
      AppCard(appInfo) { onAppClick(appInfo) }
    }
    item {
      Spacer(modifier = Modifier.statusBarsPadding())
    }
  }
}