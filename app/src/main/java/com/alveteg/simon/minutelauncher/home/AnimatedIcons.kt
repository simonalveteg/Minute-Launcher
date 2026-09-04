package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@Composable
fun AnimatedFavoriteIcon(favorite: Boolean) {
  val rotation = remember { Animatable(0f) }
  val scale = remember { Animatable(1f) }

  LaunchedEffect(favorite) {
    if (favorite) {
      launch {
        scale.animateTo(
          targetValue = 1.15f,
          animationSpec = tween(100, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
          targetValue = 1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
      }
      launch {
        rotation.animateTo(
          targetValue = 0f,
          animationSpec = keyframes {
            durationMillis = 350
            0f at 0
            2f at 60
            (-1f) at 140
            1f at 220
            0f at 350
          }
        )
      }
    } else {
      launch {
        scale.animateTo(
          targetValue = 0.9f,
          animationSpec = tween(100, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
          targetValue = 1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
      }
    }
  }

  Icon(
    imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
    contentDescription = if (favorite) "Unfavorite" else "Favorite",
    modifier = Modifier.graphicsLayer {
      scaleX = scale.value
      scaleY = scale.value
      rotationZ = rotation.value
    }
  )
}