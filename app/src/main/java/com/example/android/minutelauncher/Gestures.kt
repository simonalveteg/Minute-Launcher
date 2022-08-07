package com.example.android.minutelauncher

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable
import kotlin.math.abs

enum class GestureZone {
  UPPER, LOWER
}

enum class GestureDirection {
  LEFT, RIGHT, DOWN, UP
}

@Serializable
data class GestureAction(
  val direction: GestureDirection,
  val zone: GestureZone,
)

fun gestureHandler(dragAmount: Offset, threshold: Float, gestureZone: GestureZone): GestureAction? {
  var gestureAction: GestureAction? = null
  if (abs(dragAmount.x) > abs(dragAmount.y)) {
    if (abs(dragAmount.x) > threshold) {
      if (dragAmount.x > 0) {
        gestureAction = GestureAction(GestureDirection.RIGHT, gestureZone)
      } else if (dragAmount.x < 0) {
        gestureAction = GestureAction(GestureDirection.LEFT, gestureZone)
      }
    }
  } else if (abs(dragAmount.x) < abs(dragAmount.y)) {
    if (abs(dragAmount.y) > threshold) {
      if (dragAmount.y > 0) {
        gestureAction = GestureAction(GestureDirection.DOWN, gestureZone)
      } else if (dragAmount.y < 0) {
        gestureAction = GestureAction(GestureDirection.UP, gestureZone)
      }
    }
  }
  return gestureAction
}