package com.alveteg.simon.minutelauncher

import android.content.Intent
import com.alveteg.simon.minutelauncher.data.AppInfo

sealed class UiEvent {
  data class ShowToast(val text: String) : UiEvent()
  data object VibrateLongPress : UiEvent()
  data class LaunchActivity(val intent: Intent) : UiEvent()
  data object ExpandNotifications : UiEvent()
  data class ShowModal(val appInfo: AppInfo) : UiEvent()
}
