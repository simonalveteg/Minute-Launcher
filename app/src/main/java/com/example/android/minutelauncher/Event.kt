package com.example.android.minutelauncher

import android.content.Intent


sealed class Event {
  data class OpenApplication(val app: UserApp) : Event()
  data class LaunchActivity(val app: UserApp, val intent: Intent) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  data class ToggleFavorite(val app: UserApp) : Event()
  data class HandleGesture(val gesture: GestureDirection) : Event()
  data class SetAppGesture(val app: UserApp, val gesture: GestureDirection) : Event()
  data class ClearAppGesture(val gesture: GestureDirection) : Event()
}
