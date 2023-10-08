package com.example.android.minutelauncher

import android.content.Intent
import com.example.android.minutelauncher.db.App

sealed class UiEvent {
  data class ShowToast(val text: String) : UiEvent()
  data class OpenApplication(val app: App) : UiEvent()
  data class LaunchActivity(val intent: Intent) : UiEvent()
  object OpenAppDrawer : UiEvent()
  object ExpandNotifications : UiEvent()
}
