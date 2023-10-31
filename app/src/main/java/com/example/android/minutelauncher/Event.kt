package com.example.android.minutelauncher

import com.example.android.minutelauncher.db.App
import com.example.android.minutelauncher.db.FavoriteAppWithApp


sealed class Event {
  data class OpenApplication(val app: App) : Event()
  data class LaunchActivity(val app: App) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  data class ToggleFavorite(val app: App) : Event()
  data class HandleGesture(val gesture: GestureDirection) : Event()
  data class SetAppGesture(val app: App, val gesture: GestureDirection) : Event()
  data class ClearAppGesture(val gesture: GestureDirection) : Event()
  data class UpdateFavoriteOrder(val favorites: List<FavoriteAppWithApp>) : Event()
  data class ChangeScreenState(val state: ScreenState) : Event()
}
