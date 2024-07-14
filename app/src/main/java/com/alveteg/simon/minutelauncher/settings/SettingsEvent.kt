package com.alveteg.simon.minutelauncher.settings

import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.utilities.Gesture

sealed class SettingsEvent : Event {

  data class SetDefaultTimer(val accessTimerMapping: AccessTimerMapping) : SettingsEvent()
  data class ClearAppGesture(val gesture: Gesture) : SettingsEvent()
  data class SetAppGesture(val app: App, val gesture: Gesture) : SettingsEvent()
  data class OpenGestureList(val gesture: Gesture) : SettingsEvent()
}
