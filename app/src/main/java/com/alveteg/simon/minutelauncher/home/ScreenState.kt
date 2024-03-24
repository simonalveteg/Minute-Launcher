package com.alveteg.simon.minutelauncher.home

enum class ScreenState {
  FAVORITES, DASHBOARD, APPS, SELECTOR;

  fun isFavorites() = this == FAVORITES
  fun isSelector() = this == SELECTOR
  fun isDashboard() = this == DASHBOARD
}