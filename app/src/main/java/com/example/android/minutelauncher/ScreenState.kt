package com.example.android.minutelauncher

enum class ScreenState {
  FAVORITES, APPS, MODIFY;

  fun isFavorites() = this == FAVORITES
  fun isModify() = this == MODIFY

  fun toggleModify(): ScreenState {
    return when (this) {
      FAVORITES -> MODIFY
      MODIFY -> FAVORITES
      else -> this
    }
  }

}