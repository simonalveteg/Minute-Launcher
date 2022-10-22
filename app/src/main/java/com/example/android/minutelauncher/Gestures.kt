package com.example.android.minutelauncher

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

enum class GestureZone {
  UPPER, LOWER
}

enum class GestureDirection {
  UPPER_LEFT, LOWER_LEFT, UPPER_RIGHT, LOWER_RIGHT, DOWN, UP
}

@Serializable
data class GestureAction(
  val direction: GestureDirection,
  val zone: GestureZone,
)

fun gestureHandler(dragAmount: Offset, threshold: Float, gestureZone: GestureZone): GestureDirection? {
  var gestureDirection: GestureDirection? = null
  if (abs(dragAmount.x) > abs(dragAmount.y)) {
    if (abs(dragAmount.x) > threshold) {
      if (dragAmount.x > 0) {
        gestureDirection = if (gestureZone == GestureZone.UPPER) GestureDirection.UPPER_RIGHT else GestureDirection.LOWER_RIGHT
      } else if (dragAmount.x < 0) {
        gestureDirection = if (gestureZone == GestureZone.UPPER) GestureDirection.UPPER_LEFT else GestureDirection.LOWER_LEFT
      }
    }
  } else if (abs(dragAmount.x) < abs(dragAmount.y)) {
    if (abs(dragAmount.y) > threshold) {
      if (dragAmount.y > 0) {
        gestureDirection = GestureDirection.DOWN
      } else if (dragAmount.y < 0) {
        gestureDirection = GestureDirection.UP
      }
    }
  }
  return gestureDirection
}