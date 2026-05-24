package com.alveteg.simon.minutelauncher

import android.content.Intent
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.utilities.Gesture

sealed class UiEvent {
  data class ShowToast(val text: String) : UiEvent()
  data object VibrateLongPress : UiEvent()
  data class LaunchActivity(val intent: Intent) : UiEvent()
  data object ExpandNotifications : UiEvent()
  data object ShowDashboard : UiEvent()
  data class ShowModal(val appInfo: AppInfo) : UiEvent()
  data class TriggerGesture(val gesture: Gesture, val appInfo: AppInfo?) : UiEvent()
  data class Navigate(val route: String, val popBackStack: Boolean = false): UiEvent()
}
