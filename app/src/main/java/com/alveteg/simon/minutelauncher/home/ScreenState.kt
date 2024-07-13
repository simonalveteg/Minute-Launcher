package com.alveteg.simon.minutelauncher.home

enum class ScreenState {
  FAVORITES, DASHBOARD, APPS;

  fun isFavorites() = this == FAVORITES
  fun isDashboard() = this == DASHBOARD
}