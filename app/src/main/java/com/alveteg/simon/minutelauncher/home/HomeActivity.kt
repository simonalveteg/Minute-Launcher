package com.alveteg.simon.minutelauncher.home

import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.alveteg.simon.minutelauncher.settings.SettingsActivity
import com.alveteg.simon.minutelauncher.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MinuteLauncherTheme {
        if (!isAccessGranted(LocalContext.current)) {
          // TODO: open dialog informing user about permission before opening settings
          startActivity(Intent().apply {
            action = Settings.ACTION_USAGE_ACCESS_SETTINGS
            flags += Intent.FLAG_ACTIVITY_NEW_TASK
          })
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
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