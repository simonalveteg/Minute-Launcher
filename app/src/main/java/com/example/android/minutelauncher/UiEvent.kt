package com.example.android.minutelauncher

import android.content.Intent

sealed class UiEvent {
  data class ShowToast(val text: String) : UiEvent()
  data class OpenApplication(val app: UserApp) : UiEvent()
  data class LaunchActivity(val intent: Intent) : UiEvent()
  object OpenAppDrawer : UiEvent()
}
