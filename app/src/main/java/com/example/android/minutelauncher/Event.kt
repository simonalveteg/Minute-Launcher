package com.example.android.minutelauncher


sealed class Event {
  data class OpenApplication(val app: UserApp) : Event()
  data class UpdateSearch(val searchTerm: String) : Event()
  object CloseAppsList : Event()
  object OpenAppsList : Event()
  data class ToggleFavorite(val app: UserApp) : Event()
  data class ShowAppInfo(val app: UserApp) : Event()
  object DismissDialog : Event()
  object SearchClicked : Event()
  object DismissSearch : Event()
  data class SwipeLeft(val gestureZone: GestureZone) : Event()
  data class SwipeRight(val gestureZone: GestureZone) : Event()
  object SwipeUp : Event()
  object SwipeDown : Event()
  object NavigateToSettings : Event()
}
