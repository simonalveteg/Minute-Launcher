package com.alveteg.simon.minutelauncher.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.theme.AppTheme
import com.alveteg.simon.minutelauncher.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

  @Inject
  lateinit var userPreferencesRepository: PreferenceRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appTheme by userPreferencesRepository.appTheme.collectAsState(initial = AppTheme.DARK)
      val useDynamicColor by userPreferencesRepository.useDynamicColor.collectAsState(initial = true)

      MinuteLauncherTheme(
        themePreference = appTheme,
        dynamicColor = useDynamicColor
      ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val destination = intent.getStringExtra("screen")
        if (destination == null) {
          Toast.makeText(this, "Destination missing.", Toast.LENGTH_SHORT).show()
          return@MinuteLauncherTheme
        }
        SettingsNavHost(navController = rememberNavController(), startDestination = destination)
      }
    }
  }
}

object SettingsScreen {
  const val HOME = "home"
  const val GESTURE_SETTINGS_LIST = "gestures_list"
  const val TIMER_SETTINGS = "timer_settings"
  const val BACK = "back"
}
