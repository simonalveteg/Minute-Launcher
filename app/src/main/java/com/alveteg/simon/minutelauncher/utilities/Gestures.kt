package com.alveteg.simon.minutelauncher.utilities

enum class GestureZone {
  UPPER, LOWER, NONE, INVALID
}

enum class GestureDirection {
  LEFT, RIGHT, NONE, INVALID
}

enum class Gesture {
  UPPER_LEFT, LOWER_LEFT, UPPER_RIGHT, LOWER_RIGHT, DOWN, UP, NONE;

  companion object {
    fun from(zone: GestureZone, direction: GestureDirection): Gesture {
      return when (zone) {
        GestureZone.UPPER -> {
          when (direction) {
            GestureDirection.RIGHT -> UPPER_RIGHT
            GestureDirection.LEFT -> UPPER_LEFT
            else -> NONE
          }
        }

        GestureZone.LOWER -> {
          when (direction) {
            GestureDirection.RIGHT -> LOWER_RIGHT
            GestureDirection.LEFT -> LOWER_LEFT
            else -> NONE
          }
        }

        else -> NONE
      }
    }
  }
}
