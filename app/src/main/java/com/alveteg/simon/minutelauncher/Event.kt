package com.alveteg.simon.minutelauncher

import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.FavoriteAppWithApp
import com.alveteg.simon.minutelauncher.home.ScreenState
import com.alveteg.simon.minutelauncher.utilities.Gesture


sealed class Event {
  data class OpenApplication(val app: App) : Event()
  data class LaunchActivity(val app: App) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  data class ToggleFavorite(val app: App) : Event()
  data class HandleGesture(val gesture: Gesture) : Event()
  data class SetAppGesture(val app: App, val gesture: Gesture) : Event()
  data class ClearAppGesture(val gesture: Gesture) : Event()
  data class UpdateFavoriteOrder(val favorites: List<FavoriteAppWithApp>) : Event()
  data class ChangeScreenState(val state: ScreenState) : Event()
  data class UpdateApp(val app: App) : Event()
  object ClearModal : Event()
}
