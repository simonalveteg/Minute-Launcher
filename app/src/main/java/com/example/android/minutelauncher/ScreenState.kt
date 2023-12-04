package com.example.android.minutelauncher

enum class ScreenState {
  FAVORITES, APPS, MODIFY, SELECTOR;

  fun isFavorites() = this == FAVORITES
  fun isModify() = this == MODIFY
  fun isSelector() = this == SELECTOR
  fun isApps() = this == APPS
  fun hasSearch() = this == APPS || this == SELECTOR

  fun toggleModify(): ScreenState {
    return when (this) {
      FAVORITES -> MODIFY
      MODIFY -> FAVORITES
      else -> this
    }
  }
}