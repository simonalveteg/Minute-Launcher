package com.alveteg.simon.minutelauncher.home

enum class ScreenState {
  FAVORITES, DASHBOARD;

  fun isFavorites() = this == FAVORITES
  fun isDashboard() = this == DASHBOARD
}