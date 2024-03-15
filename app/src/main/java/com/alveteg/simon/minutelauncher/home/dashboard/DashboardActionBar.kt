package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.runtime.Composable
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction

@Composable
fun DashboardActionBar(
  onEvent: (Event) -> Unit
) {

  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Default.Settings,
      description = "Open system settings",
      action = {}
    ),
    ActionBarAction(
      imageVector = Icons.Default.Gesture,
      description = "Change gesture shortcuts",
      action = {}
    ),
    ActionBarAction(
      imageVector = Icons.Default.HourglassEmpty,
      description = "Open Digital Wellbeing",
      action = {}
    ),
    ActionBarAction(
      imageVector = Icons.Default.Wallpaper,
      description = "Change Wallpaper",
      action = {}
    ),
    ActionBarAction(
      imageVector = Icons.Default.RestartAlt,
      description = "Restart Minute Launcher",
      action = {}
    ),
    ActionBarAction(
      imageVector = Icons.Default.Feedback,
      description = "Send Feedback",
      action = {}
    ),
  )

  ActionBar(actions = actions)
}