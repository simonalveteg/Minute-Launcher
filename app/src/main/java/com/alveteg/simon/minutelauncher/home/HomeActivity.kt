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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alveteg.simon.minutelauncher.MinuteDeviceAdminReceiver
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.settings.SettingsActivity
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
    window.isNavigationBarContrastEnforced = false
    setContent {
      val appTheme by userPreferencesRepository.appTheme.collectAsState(initial = PreferenceRepository.Defaults.APP_THEME)
      val useDynamicColor by userPreferencesRepository.useDynamicColor.collectAsState(initial = PreferenceRepository.Defaults.USE_DYNAMIC_COLOR)

      val isUsageGranted by produceState(initialValue = isUsageAccessGranted(this)) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            value = isUsageAccessGranted(this@HomeActivity)
          }
        }
        lifecycle.addObserver(observer)
        awaitDispose { lifecycle.removeObserver(observer) }
      }

      val isDefaultLauncher by produceState(initialValue = isDefaultLauncher(this)) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            value = isDefaultLauncher(this@HomeActivity)
          }
        }
        lifecycle.addObserver(observer)
        awaitDispose { lifecycle.removeObserver(observer) }
      }

      val isDeviceAdmin by produceState(initialValue = isDeviceAdmin(this)) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            value = isDeviceAdmin(this@HomeActivity)
          }
        }
        lifecycle.addObserver(observer)
        awaitDispose { lifecycle.removeObserver(observer) }
      }

      val systemPermissions = SystemPermissions(
        isUsageGranted = isUsageGranted,
        isDefaultLauncher = isDefaultLauncher,
        isDeviceAdmin = isDeviceAdmin
      )

      MinuteLauncherTheme(
        themePreference = appTheme,
        dynamicColor = useDynamicColor
      ) {
        CompositionLocalProvider(LocalSystemPermissions provides systemPermissions) {
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
}

data class SystemPermissions(
  val isUsageGranted: Boolean = false,
  val isDefaultLauncher: Boolean = false,
  val isDeviceAdmin: Boolean = false
)

val LocalSystemPermissions = staticCompositionLocalOf { SystemPermissions() }

fun isUsageAccessGranted(context: Context): Boolean {
  val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
  val mode = appOps.checkOpNoThrow(
    AppOpsManager.OPSTR_GET_USAGE_STATS,
    android.os.Process.myUid(),
    context.packageName
  )
  return mode == AppOpsManager.MODE_ALLOWED
}

fun isDeviceAdmin(context: Context): Boolean {
  val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
  val adminComponent = ComponentName(context, MinuteDeviceAdminReceiver::class.java)
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