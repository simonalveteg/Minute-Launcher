package com.alveteg.simon.minutelauncher.utilities

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
}
