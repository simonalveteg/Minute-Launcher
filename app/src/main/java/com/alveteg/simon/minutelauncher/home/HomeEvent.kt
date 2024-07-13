package com.alveteg.simon.minutelauncher.home

import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.FavoriteAppInfo
import com.alveteg.simon.minutelauncher.utilities.Gesture


sealed class HomeEvent : Event {
  data class OpenApplication(val appInfo: AppInfo) : HomeEvent()
  data class LaunchActivity(val appInfo: AppInfo) : HomeEvent()
  data class UpdateSearch(val searchTerm: String) : HomeEvent()
  data class ToggleFavorite(val app: App) : HomeEvent()
  data class HandleGesture(val gesture: Gesture) : HomeEvent()
  data class UpdateFavoriteOrder(val favorites: List<FavoriteAppInfo>) : HomeEvent()
  data class UpdateApp(val app: App) : HomeEvent()
  data object OpenGestureSettings : HomeEvent()
  data object OpenTimerSettings : HomeEvent()
}
