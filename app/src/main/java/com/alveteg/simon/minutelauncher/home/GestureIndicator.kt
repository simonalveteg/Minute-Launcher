package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Composable
fun BoxScope.GestureIndicator(
  dragProgress: Float,
  isTriggered: Boolean,
  activeGesture: Gesture,
  modifier: Modifier = Modifier
) {
  val animatedWidth by animateFloatAsState(
    targetValue = dragProgress, label = "GestureIndicator"
  )
  val popScale by animateFloatAsState(
    targetValue = if (isTriggered) 1.5f else 0.8f,
    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
    label = "PopAnimation"
  )

  val indicatorColor by animateColorAsState(
    targetValue = if (isTriggered) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), label = "IndicatorColor"
  )

  if (animatedWidth > 0.05f) {

    val alignment = when (activeGesture) {
      Gesture.TOP_LEFT -> Alignment.TopStart
      Gesture.TOP_RIGHT -> Alignment.TopEnd
      Gesture.BOTTOM_LEFT -> Alignment.BottomStart
      Gesture.BOTTOM_RIGHT -> Alignment.BottomEnd
      else -> Alignment.Center
    }

    val shape = if (activeGesture.isLeft()) {
      RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
    } else {
      RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
    }

    Box(
      modifier = modifier
        .fillMaxHeight(0.5f)
        .width((24.dp * animatedWidth) * popScale)
        .align(alignment)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight(0.6f)
          .fillMaxWidth()
          .align(Alignment.Center)
          .background(
            color = indicatorColor, shape = shape
          )
      )
    }
  }
}