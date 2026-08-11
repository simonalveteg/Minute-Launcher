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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction
import com.alveteg.simon.minutelauncher.home.dashboard.launchDigitalWellbeing


@Composable
fun AppModalActionBar(
  appInfo: AppInfo,
  enabled: Boolean,
  onChangeDelay: () -> Unit,
  onEditName: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current

  val favoriteIcon = if (appInfo.favorite) Icons.Filled.Star else Icons.Filled.StarBorder
  val favoriteText = if (appInfo.favorite) R.string.description_remove_favorite else R.string.description_add_favorite
  val wellbeingNotFound = stringResource(R.string.error_digital_wellbeing_not_found)

  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Outlined.Delete,
      description = stringResource(R.string.description_uninstall_app),
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
      description = stringResource(R.string.description_change_mindful_delay),
      action = onChangeDelay,
      enabled = enabled
    ),
    ActionBarAction(
      imageVector = ImageVector.vectorResource(id = R.drawable.digital_wellbeing),
      description = stringResource(R.string.description_show_app_usage_details),
      action = {
        val intent = Intent().apply {
          action = Settings.ACTION_APP_USAGE_SETTINGS
          flags += Intent.FLAG_ACTIVITY_NEW_TASK
          putExtra(Intent.EXTRA_PACKAGE_NAME, appInfo.app.packageName)
        }
        try {
          mContext.startActivity(intent, null)
        } catch (e: Exception) {
          onEvent(HomeEvent.ShowToast(wellbeingNotFound))
        }
      }
    ),
    ActionBarAction(
      imageVector = favoriteIcon,
      description = stringResource(favoriteText),
      action = { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) },
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = stringResource(R.string.action_open_app_info),
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
      description = stringResource(R.string.description_edit_app_name),
      action = { onEditName() },
    ),
  )

  ActionBar(actions = actions)
}
