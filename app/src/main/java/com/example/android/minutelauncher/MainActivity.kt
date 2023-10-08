package com.example.android.minutelauncher

import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.android.minutelauncher.ui.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Timber.plant(Timber.DebugTree())
    setContent {
      MinuteLauncherTheme {
        val navController = rememberNavController()
        if (!isAccessGranted(LocalContext.current)) {
          // TODO: open dialog informing user about permission before opening settings
          startActivity(Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS })
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        LauncherNavHost(navController)
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