package com.alveteg.simon.minutelauncher.home.dashboard

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction
import com.alveteg.simon.minutelauncher.home.ActionBarState
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.utilities.launchIntent


@Composable
fun DashboardActionBar(
  actionBarState: ActionBarState,
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current
  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Default.Settings,
      description = "Open system settings",
      action = {
        val intent = Intent(Settings.ACTION_SETTINGS)
        launchIntent(context = mContext, intent = intent)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Gesture,
      description = "Change gesture shortcuts",
      action = { onEvent(HomeEvent.OpenGestureSettings) }
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
        launchIntent(context = mContext, intent = intent, onNoActivityFound = {
          onEvent(
            HomeEvent.ShowToast(
              text = "Couldn't find Digital Wellbeing.",
              length = Toast.LENGTH_LONG
            )
          )
        })
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Wallpaper,
      description = "Change Wallpaper",
      action = {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        launchIntent(mContext, intent)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Timer,
      description = "Change Timer Settings",
      action = { onEvent(HomeEvent.OpenTimerSettings) }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = "Open App Info",
      action = {
        val intent = Intent().apply {
          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          data = Uri.fromParts("package", mContext.packageName, null)
        }
        launchIntent(context = mContext, intent = intent)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Feedback,
      description = "Send Feedback",
      action = {}
    ),
  )

  ActionBar(state = actionBarState, actions = actions)
}