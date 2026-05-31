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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.BuildConfig
import com.alveteg.simon.minutelauncher.utilities.Gesture
import timber.log.Timber

@Composable
fun BoxScope.GestureIndicator(
  dragProgress: Float,
  isTriggered: Boolean,
  activeGesture: Gesture,
  fingerPosition: Offset,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  val configuration = LocalConfiguration.current
  val screenHeightDp = configuration.screenHeightDp.dp
  val verticalPadding = screenHeightDp.div(12)
  val defaultWidth = 30.dp

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
    targetValue = defaultWidth * dragProgress,
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
  val contentColor by animateColorAsState(
    targetValue = if (isTriggered) {
      MaterialTheme.colorScheme.onPrimary
    } else {
      Color.Transparent
    },
    animationSpec = tween(150),
    label = "IndicatorContentColor"
  )

  if (finalWidth > 0.1.dp && activeGesture != Gesture.NONE) {
    val alignment = when (activeGesture) {
      Gesture.TOP_LEFT -> Alignment.TopStart
      Gesture.TOP_RIGHT -> Alignment.TopEnd
      Gesture.BOTTOM_LEFT -> Alignment.BottomStart
      Gesture.BOTTOM_RIGHT -> Alignment.BottomEnd
      else -> Alignment.Center
    }

    var indicatorOffset by remember { mutableStateOf(Offset.Zero) }
    var iconVerticalShift by remember { mutableStateOf(0.dp) }

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
          .onGloballyPositioned { coords ->
            indicatorOffset = coords.positionInRoot()
          }
          .drawWithCache {
            val isLeft = activeGesture.isLeft()
            val startX = if (isLeft) 0f else size.width
            val centerY = size.height / 2f
            val bulgeX = if (isLeft) size.width else 0f
            val midX = (startX + bulgeX) / 2f

            val localFingerPosition = fingerPosition - indicatorOffset - Offset(startX, centerY)

            fun findVerticalOffset(x: Float): Float {
              if (localFingerPosition.x == 0f) return centerY
              val k = localFingerPosition.y / localFingerPosition.x
              val dx = x - startX
              return k * dx + centerY
            }

            val bulgeY = findVerticalOffset(bulgeX)
            val verticalOffset = localFingerPosition.y.times(0.1f)

            iconVerticalShift = with(density) {
              val iconOffset = findVerticalOffset(bulgeX.div(2)) - centerY + verticalOffset
              Timber.d("Offset: $iconOffset")
              iconOffset.toDp()
            }

            onDrawBehind {
              val path = Path().apply {

                moveTo(startX, verticalOffset + 0f)
                cubicTo(
                  midX, verticalOffset + 0f,
                  bulgeX, verticalOffset + bulgeY - size.height * 0.3f,
                  bulgeX, verticalOffset + bulgeY
                )
                cubicTo(
                  bulgeX, verticalOffset + bulgeY + size.height * 0.3f,
                  midX, verticalOffset + size.height,
                  startX, verticalOffset + size.height
                )
                close()
              }
              drawPath(path, color = indicatorColor)
              if (BuildConfig.DEBUG) {
                drawLine(
                  color = Color.Red,
                  start = Offset(startX, verticalOffset + centerY),
                  end = Offset(bulgeX, verticalOffset + bulgeY),
                  strokeWidth = 4f
                )
              }
            }
          }
      ) {
        Icon(
          painter = painterResource(activeGesture.getIcon()),
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier
            .align(Alignment.Center)
            .offset(y = iconVerticalShift)
        )
      }
    }
  }
}