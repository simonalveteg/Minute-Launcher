package com.alveteg.simon.minutelauncher.utilities

import com.alveteg.simon.minutelauncher.R

enum class GestureZone {
  UPPER, LOWER, NONE, INVALID
}

enum class GestureDirection {
  LEFT, RIGHT, NONE, INVALID
}
enum class Gesture {
  TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT, DOWN, UP, NONE;

  fun isLeft() = this == TOP_LEFT || this == BOTTOM_LEFT
  fun isRight() = this == TOP_RIGHT || this == BOTTOM_RIGHT
  fun isTop() = this == TOP_LEFT || this == TOP_RIGHT
  fun isBottom() = this == BOTTOM_LEFT || this == BOTTOM_RIGHT

  fun getIcon(): Int {
    return when (this) {
      TOP_RIGHT -> R.drawable.gesture_top_right
      BOTTOM_RIGHT -> R.drawable.gesture_bottom_right
      TOP_LEFT -> R.drawable.gesture_top_left
      BOTTOM_LEFT -> R.drawable.gesture_bottom_left
      else -> R.drawable.gesture_top_right
    }
  }

  companion object {
    fun getHorizontalGestures(): List<Gesture> {
      return listOf(TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT).sortedByDescending { it.name }
    }
  }
}
