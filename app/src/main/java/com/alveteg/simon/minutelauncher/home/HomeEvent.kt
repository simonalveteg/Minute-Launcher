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
  data class UpdateFavoriteOrder(val from: Int, val to: Int) : HomeEvent()
  data class UpdateAppTimer(val app: App, val timerValue: Int) : HomeEvent()
  data class ResetAppTimerToDefault(val app: App) : HomeEvent()
  data class SetDisplayName(val app: App, val displayName: String) : HomeEvent()
  data class ResetDisplayName(val app: App) : HomeEvent()
  data object OpenTimerSettings : HomeEvent()
  data object OpenSettings : HomeEvent()
  data object HidePermissionPrompts : HomeEvent()
}
