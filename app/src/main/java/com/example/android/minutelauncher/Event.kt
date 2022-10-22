package com.example.android.minutelauncher


sealed class Event {
  data class OpenApplication(val app: UserApp) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  data class ToggleFavorite(val app: UserApp) : Event()
  data class HandleGesture(val gesture: GestureAction) : Event()
  data class SetAppGesture(val app: UserApp) : Event()
}
