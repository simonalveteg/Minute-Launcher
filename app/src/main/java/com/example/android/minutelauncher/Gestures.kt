package com.example.android.minutelauncher

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

enum class GestureZone {
  UPPER, LOWER
}

fun gestureHandler(dragAmount: Offset, threshold: Float, gestureZone: GestureZone): Event? {
  var gestureEvent: Event? = null
  if (abs(dragAmount.x) > abs(dragAmount.y)) {
    if (abs(dragAmount.x) > threshold) {
      if (dragAmount.x > 0) {
        gestureEvent = Event.SwipeRight(gestureZone)
      } else if (dragAmount.x < 0) {
        gestureEvent = Event.SwipeLeft(gestureZone)
      }
    }
  } else if (abs(dragAmount.x) < abs(dragAmount.y)) {
    if (abs(dragAmount.y) > threshold) {
      if (dragAmount.y > 0) {
        gestureEvent = Event.SwipeDown
      } else if (dragAmount.y < 0) {
        gestureEvent = Event.SwipeUp
      }
    }
  }
  return gestureEvent
}