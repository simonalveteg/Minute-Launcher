package com.alveteg.simon.minutelauncher.home.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.BuildConfig
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction
import com.alveteg.simon.minutelauncher.home.HomeActivity
import timber.log.Timber


@Composable
fun DashboardActionBar(
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current
  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Default.Settings,
      description = "Open system settings",
      action = {
        val intent = Intent(Settings.ACTION_SETTINGS)
        mContext.startActivity(intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Wallpaper,
      description = "Change Wallpaper",
      action = {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
          mContext.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
        } catch (e: Exception) {
          Timber.e(e, "No wallpaper app found")
        }
      }
    ),
    ActionBarAction(
      imageVector = ImageVector.vectorResource(id = R.drawable.digital_wellbeing),
      description = "Open Digital Wellbeing",
      action = {
        val intent = Intent().apply {
          setClassName(
            "com.google.android.apps.wellbeing",
            "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
          )
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mContext.startActivity(intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Tune,
      description = "Open launcher settings",
      action = { onEvent(HomeEvent.OpenSettings) }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = "Open App Info",
      action = {
        val intent = Intent().apply {
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          data = Uri.fromParts("package", mContext.packageName, null)
        }
        mContext.startActivity(intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Feedback,
      description = "Send Feedback",
      action = {
        val body = """
        |
        |
        |------------------------------------------------------
        |Launcher Version: ${BuildConfig.VERSION_NAME}
        |Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        |Device: ${Build.MANUFACTURER} ${Build.MODEL}
        |------------------------------------------------------
        """.trimMargin()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
          data = Uri.parse("mailto:")
          putExtra(Intent.EXTRA_EMAIL, arrayOf("dev@simonalveteg.com"))
          putExtra(Intent.EXTRA_SUBJECT, "Feedback: Minute Launcher")
          putExtra(Intent.EXTRA_TEXT, body)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
          mContext.startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: Exception) {
          Timber.e(e, "No email app found")
        }
      }
    ),
  )

  ActionBar(actions = actions)
}