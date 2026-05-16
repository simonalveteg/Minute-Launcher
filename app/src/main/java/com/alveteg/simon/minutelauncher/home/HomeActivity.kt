package com.alveteg.simon.minutelauncher.home

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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

fun isUsageAccessGranted(context: Context): Boolean {
  val appOpsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
  return appOpsManager.unsafeCheckOpNoThrow(
    "android:get_usage_stats",
    android.os.Process.myUid(), context.packageName
  ) == AppOpsManager.MODE_ALLOWED
}

fun isDeviceAdmin(context: Context): Boolean {
  val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
  val adminComponent = ComponentName(context, "com.alveteg.simon.minutelauncher.MinuteDeviceAdminReceiver")
  return dpm.isAdminActive(adminComponent)
}

fun isDefaultLauncher(context: Context): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    roleManager.isRoleHeld(RoleManager.ROLE_HOME)
  } else {
    false
  }
}