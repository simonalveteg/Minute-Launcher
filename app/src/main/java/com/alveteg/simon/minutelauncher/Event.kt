package com.alveteg.simon.minutelauncher

import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.FavoriteAppInfo
import com.alveteg.simon.minutelauncher.utilities.Gesture


sealed class Event {
  data class OpenApplication(val appInfo: AppInfo) : Event()
  data class LaunchActivity(val appInfo: AppInfo) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  data class ToggleFavorite(val app: App) : Event()
  data class HandleGesture(val gesture: Gesture) : Event()
  data class SetAppGesture(val app: App, val gesture: Gesture) : Event()
  data class ClearAppGesture(val gesture: Gesture) : Event()
  data class UpdateFavoriteOrder(val favorites: List<FavoriteAppInfo>) : Event()
  data class UpdateApp(val app: App) : Event()
  data object OpenGestureSettings : Event()
  data class OpenGestureList(val gesture: Gesture) : Event()
  data object OpenTimerSettings : Event()
  data class SetDefaultTimer(val accessTimerMapping: AccessTimerMapping) : Event()
}
