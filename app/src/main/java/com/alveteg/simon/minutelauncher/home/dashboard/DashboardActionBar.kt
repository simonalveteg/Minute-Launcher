package com.alveteg.simon.minutelauncher.home.dashboard

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction


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
        ContextCompat.startActivity(mContext, intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Gesture,
      description = "Change gesture shortcuts",
      action = { onEvent(Event.OpenGestures) }
    ),
    ActionBarAction(
      imageVector = ImageVector.vectorResource(id = R.drawable.digital_wellbeing),
      description = "Open Digital Wellbeing",
      action = {
        val intent = Intent()
        intent.setClassName(
          "com.google.android.apps.wellbeing",
          "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
        )
        ContextCompat.startActivity(mContext, intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Wallpaper,
      description = "Change Wallpaper",
      action = {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        ContextCompat.startActivity(
          mContext,
          Intent.createChooser(intent, "Select Wallpaper"),
          null
        )
      }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = "Open App Info",
      action = {
        val intent = Intent().apply {
          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          data = Uri.fromParts("package", mContext.packageName, null)
        }
        ContextCompat.startActivity(mContext, intent, null)
      }
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