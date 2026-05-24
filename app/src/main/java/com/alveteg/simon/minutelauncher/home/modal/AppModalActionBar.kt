package com.alveteg.simon.minutelauncher.home.modal

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction
import timber.log.Timber


@Composable
fun AppModalActionBar(
  appInfo: AppInfo,
  enabled: Boolean,
  onChangeTimer: () -> Unit,
  onEditName: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current

  val favoriteIcon = if (appInfo.favorite) Icons.Filled.Star else Icons.Filled.StarBorder
  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Outlined.Delete,
      description = "Uninstall app",
      action = {
        val intent = Intent().apply {
          action = Intent.ACTION_DELETE
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
          data = Uri.fromParts("package", appInfo.app.packageName, null)
        }
        mContext.startActivity(intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Timer,
      description = "Change app timer",
      action = onChangeTimer,
      enabled = enabled
    ),
    ActionBarAction(
      imageVector = ImageVector.vectorResource(id = R.drawable.digital_wellbeing),
      description = "Show app usage details",
      action = {
        val intent = Intent().apply {
          action = Settings.ACTION_APP_USAGE_SETTINGS
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
          putExtra(Intent.EXTRA_PACKAGE_NAME, appInfo.app.packageName)
        }
        try {
          mContext.startActivity(intent, null)
        } catch (e: Exception) {
          onEvent(HomeEvent.ShowToast("Could not open app usage settings. Digital Wellbeing might not be present on your device."))
        }
      }
    ),
    ActionBarAction(
      imageVector = favoriteIcon,
      description = "Toggle app favorite",
      action = { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) },
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = "Open app info",
      action = {
        val intent = Intent().apply {
          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
          data = Uri.fromParts("package", appInfo.app.packageName, null)
        }
        mContext.startActivity(intent, null)
      },
      enabled = enabled
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Edit,
      description = "Edit app name",
      action = { onEditName() },
    ),
  )

  ActionBar(actions = actions)
}

