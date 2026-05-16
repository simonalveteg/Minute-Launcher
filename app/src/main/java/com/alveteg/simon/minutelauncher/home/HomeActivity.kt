package com.alveteg.simon.minutelauncher.home

import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.settings.SettingsActivity
import com.alveteg.simon.minutelauncher.theme.AppTheme
import com.alveteg.simon.minutelauncher.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

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
        if (!isAccessGranted(LocalContext.current)) {
          // TODO: open dialog informing user about permission before opening settings
          startActivity(Intent().apply {
            action = Settings.ACTION_USAGE_ACCESS_SETTINGS
            flags += Intent.FLAG_ACTIVITY_NEW_TASK
          })
        }
        HomeScreen(onNavigate = {
          val intent = Intent(this, SettingsActivity::class.java)
          intent.putExtra("screen", it.route)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          startActivity(intent)
        })
      }
    }
  }
}

fun isAccessGranted(context: Context): Boolean {
  val appOpsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
  return appOpsManager.unsafeCheckOpNoThrow(
    "android:get_usage_stats",
    android.os.Process.myUid(), context.packageName
  ) == AppOpsManager.MODE_ALLOWED
}