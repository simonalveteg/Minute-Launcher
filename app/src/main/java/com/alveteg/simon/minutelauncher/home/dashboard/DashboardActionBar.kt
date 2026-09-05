package com.alveteg.simon.minutelauncher.home.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.alveteg.simon.minutelauncher.BuildConfig
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.ActionBar
import com.alveteg.simon.minutelauncher.home.ActionBarAction
import com.alveteg.simon.minutelauncher.home.HomeEvent
import timber.log.Timber
import androidx.core.net.toUri


@Composable
fun DashboardActionBar(
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current
  val wallpaperIntentTitle = stringResource(R.string.action_change_wallpaper)
  val feedbackSubject = stringResource(R.string.feedback_subject)
  val feedbackIntentTitle = stringResource(R.string.action_send_feedback)
  val wellbeingNotFound = stringResource(R.string.error_digital_wellbeing_not_found)
  val actions = listOf(
    ActionBarAction(
      imageVector = Icons.Default.Settings,
      description = stringResource(R.string.action_open_system_settings),
      action = {
        val intent = Intent(Settings.ACTION_SETTINGS)
        mContext.startActivity(intent, null)
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Wallpaper,
      description = stringResource(R.string.action_change_wallpaper),
      action = {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
          mContext.startActivity(Intent.createChooser(intent, wallpaperIntentTitle))
        } catch (e: Exception) {
          Timber.e(e)
        }
      }
    ),
    ActionBarAction(
      imageVector = ImageVector.vectorResource(id = R.drawable.digital_wellbeing),
      description = stringResource(R.string.action_open_digital_wellbeing),
      action = {
        launchDigitalWellbeing(mContext) {
          onEvent(HomeEvent.ShowToast(wellbeingNotFound))
        }
      }
    ),
    ActionBarAction(
      imageVector = Icons.Default.Tune,
      description = stringResource(R.string.action_open_launcher_settings),
      action = { onEvent(HomeEvent.OpenSettings) }
    ),
    ActionBarAction(
      imageVector = Icons.Outlined.Info,
      description = stringResource(R.string.action_open_app_info),
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
      description = stringResource(R.string.action_send_feedback),
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
          data = "mailto:".toUri()
          putExtra(Intent.EXTRA_EMAIL, arrayOf("dev@simonalveteg.com"))
          putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
          putExtra(Intent.EXTRA_TEXT, body)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
          mContext.startActivity(Intent.createChooser(intent, feedbackIntentTitle))
        } catch (e: Exception) {
          Timber.e(e)
        }
      }
    ),
  )

  ActionBar(actions = actions)
}

fun launchDigitalWellbeing(mContext: Context, onError: () -> Unit) {
  val pm = mContext.packageManager

  fun tryLaunch(packageName: String): Boolean {
    val intent = pm.getLaunchIntentForPackage(packageName) ?: return false
    return try {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      mContext.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }


  val digitalWellbeingApps = listOf(
    "com.google.android.apps.wellbeing",
    "com.samsung.android.forest",
  )

  val launched = digitalWellbeingApps.any { tryLaunch(it) }

  if (!launched) {
    onError()
  }
}
