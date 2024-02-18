package com.alveteg.simon.minutelauncher.home.modal

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.theme.archivoFamily


@Composable
fun AppModalActionBar(
  appInfo: AppInfo,
  enabled: Boolean,
  onChangeTimer: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    var actionBarState by remember { mutableStateOf(AppActionBarState.COLLAPSED) }
    val favoriteIcon = if (appInfo.favorite) Icons.Filled.Star else Icons.Filled.StarBorder
    val showMoreIcon =
      if (actionBarState == AppActionBarState.COLLAPSED) Icons.Default.ExpandMore else Icons.Default.ExpandLess
    Column(
      modifier = Modifier
        .animateContentSize()
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier
          .height(60.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = {
          val intent = Intent().apply {
            action = Intent.ACTION_DELETE
            data = Uri.fromParts("package", appInfo.app.packageName, null)
          }
          ContextCompat.startActivity(mContext, intent, null)
        }) {
          Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Uninstall app")
        }
        IconButton(enabled = enabled, onClick = {
          val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", appInfo.app.packageName, null)
          }
          ContextCompat.startActivity(mContext, intent, null)
        }) {
          Icon(imageVector = Icons.Outlined.Info, contentDescription = "Open app info")
        }
        IconButton(onClick = {
          val intent = Intent().apply {
            action = Settings.ACTION_APP_USAGE_SETTINGS
            putExtra(Intent.EXTRA_PACKAGE_NAME, appInfo.app.packageName)
          }
          ContextCompat.startActivity(mContext, intent, null)
        }) {
          Icon(
            imageVector = Icons.Outlined.HourglassEmpty,
            contentDescription = "Open app screen time"
          )
        }
        IconButton(onClick = { onEvent(Event.ToggleFavorite(appInfo.app)) }) {
          Icon(imageVector = favoriteIcon, contentDescription = "Toggle app favorite")
        }
        IconButton(onClick = { actionBarState = actionBarState.toggle() }) {
          Icon(imageVector = showMoreIcon, contentDescription = "Show more actions")
        }
      }
      if (actionBarState == AppActionBarState.EXPANDED) {
        AppActionTextButton(
          onClick = onChangeTimer,
          icon = {
            Icon(imageVector = Icons.Outlined.Timer, contentDescription = "Change app timer")
          },
          text = "Change app timer",
          enabled = enabled
        )
        AppActionTextButton(
          onClick = { /*TODO*/ },
          icon = {
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = "Edit app name"
            )
          },
          text = "Edit app name"
        )
      }
    }
  }
}

@Composable
fun AppActionTextButton(
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  text: String,
  enabled: Boolean = true
) {
  TextButton(
    enabled = enabled,
    onClick = onClick,
    colors = ButtonDefaults.textButtonColors(
      contentColor = LocalContentColor.current
    )
  ) {
    icon()
    Text(
      text = text,
      fontFamily = archivoFamily,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Start,
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 24.dp)
    )
  }
}

private enum class AppActionBarState {
  COLLAPSED, EXPANDED;

  fun toggle(): AppActionBarState {
    return when (this) {
      COLLAPSED -> EXPANDED
      EXPANDED -> COLLAPSED
    }
  }
}
