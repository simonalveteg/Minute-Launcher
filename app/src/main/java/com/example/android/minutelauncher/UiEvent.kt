package com.example.android.minutelauncher

import android.content.Intent

sealed class UiEvent {
  data class ShowToast(val text: String) : UiEvent()
  object VibrateLongPress : UiEvent()
  data class LaunchActivity(val intent: Intent) : UiEvent()
  object ExpandNotifications : UiEvent()
}
