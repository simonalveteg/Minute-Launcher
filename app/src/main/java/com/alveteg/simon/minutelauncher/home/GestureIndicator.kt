package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlin.math.abs

@Composable
fun BoxScope.GestureIndicator(
  dragProgress: Float,
  isTriggered: Boolean,
  activeGesture: Gesture,
  verticalPosition: Float,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  val configuration = LocalConfiguration.current
  val screenHeightDp = configuration.screenHeightDp.dp
  val verticalPadding = screenHeightDp.div(12)

  val popScaleHorizontal by animateFloatAsState(
    targetValue = if (isTriggered) 1.5f else 0.8f,
    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
    label = "PopAnimation"
  )
  val popScaleVertical by animateFloatAsState(
    targetValue = if (isTriggered) 1.2f else 0.8f,
    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
    label = "PopAnimation"
  )
  val baseWidth by animateDpAsState(
    targetValue = 24.dp * dragProgress,
    animationSpec = if (dragProgress == 0f) tween(durationMillis = 300) else snap(),
    label = "BaseWidth"
  )
  val finalWidth = baseWidth * popScaleHorizontal

  val animatedProgress = (baseWidth / 24.dp).coerceIn(0f, 1f)

  val indicatorColor by animateColorAsState(
    targetValue = if (isTriggered) MaterialTheme.colorScheme.primary.copy(alpha = 1f)
    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 1f),
    label = "IndicatorColor"
  )

  var startY by remember { mutableStateOf(0f) }
  if (startY == 0f && verticalPosition != 0f) {
    startY = verticalPosition
  }

  if (finalWidth > 0.1.dp && activeGesture != Gesture.NONE) {
    val screenHeightPx = with(density) { screenHeightDp.toPx() }

    val containerOffsetPx = if (activeGesture.isBottom()) screenHeightPx / 2f else 0f
    val containerHeightPx = screenHeightPx / 2f

    val startWithinContainer = (startY - containerOffsetPx) - (containerHeightPx / 2f)

    val rawOffset = (verticalPosition - startY)
    val maxOffsetPx = screenHeightPx / 2f * 0.2f

    val totalOffsetPx = startWithinContainer + rawOffset
    val normalizedOffset = if (maxOffsetPx != 0f) totalOffsetPx / maxOffsetPx else 0f
    val resistedOffsetPx = (normalizedOffset / (1f + abs(normalizedOffset))) * maxOffsetPx
    val verticalOffsetDp = with(density) { resistedOffsetPx.toDp() }

    val alignment = when (activeGesture) {
      Gesture.TOP_LEFT -> Alignment.TopStart
      Gesture.TOP_RIGHT -> Alignment.TopEnd
      Gesture.BOTTOM_LEFT -> Alignment.BottomStart
      Gesture.BOTTOM_RIGHT -> Alignment.BottomEnd
      else -> Alignment.Center
    }

    Box(
      modifier = modifier
        .fillMaxHeight(0.5f)
        .width(finalWidth)
        .padding(
          top = if (activeGesture.isTop()) verticalPadding else 0.dp,
          bottom = if (activeGesture.isBottom()) verticalPadding else 0.dp
        )
        .align(alignment)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight(animatedProgress * 0.1f + (0.5f * popScaleVertical))
          .fillMaxWidth()
          .align(Alignment.Center)
          .offset(y = verticalOffsetDp)
          .drawWithCache {
            val isLeft = activeGesture.isLeft()
            val offsetPx = verticalOffsetDp.toPx()
            val bulgeX = size.width * (if (isLeft) 1f else -1f)
            onDrawBehind {
              val path = Path().apply {
                val tipY = size.height * 0.5f + offsetPx * 0.35f

                moveTo(0f, 0f)
                cubicTo(
                  bulgeX * 0.5f, 0f,
                  bulgeX, tipY - size.height * 0.3f,
                  bulgeX, tipY
                )
                cubicTo(
                  bulgeX, tipY + size.height * 0.3f,
                  bulgeX * 0.5f, size.height,
                  0f, size.height
                )
                close()
              }
              drawPath(path, color = indicatorColor)
            }
          }
      )
    }
  }
}