package com.alveteg.simon.minutelauncher.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.alveteg.simon.minutelauncher.LauncherNavHost
import com.alveteg.simon.minutelauncher.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MinuteLauncherTheme {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val destination = intent.getStringExtra("screen")
        if (destination == null) {
          Toast.makeText(this, "Destination missing.", Toast.LENGTH_SHORT).show()
          return@MinuteLauncherTheme
        }
        LauncherNavHost(navController = rememberNavController(), startDestination = destination)
      }
    }
  }
}

object SettingsScreen {
  const val HOME = "home"
  const val GESTURE_SETTINGS = "gesture_settings"
  const val GESTURE_SETTINGS_LIST = "gestures_list"
  const val TIMER_SETTINGS = "timer_settings"
}
