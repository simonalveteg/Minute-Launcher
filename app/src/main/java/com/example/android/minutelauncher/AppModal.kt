package com.example.android.minutelauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun AppModal(
  app: UserApp,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit,
  viewModel: LauncherViewModel = hiltViewModel(),
) {
  val appUsage = viewModel.getUsageForApp(app).value
  val mContext = LocalContext.current

  Surface() {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = {
          onEvent(Event.ToggleFavorite(app))
        }) {
          Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite")
        }
        Text(
          text = app.appTitle, style = MaterialTheme.typography.h5
        )
        IconButton(onClick = {}) {
          Icon(imageVector = Icons.Default.Info, contentDescription = "App Info")
        }
      }
      Text(
        text = "${app.appTitle} used for ${appUsage.toTimeUsed(false)}",
        modifier = Modifier.padding(64.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        TextButton(onClick = onConfirmation) {
          Text(text = "Open anyway")
        }
        Button(onClick = {
          val isAccessibilityServiceEnabled =
            isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)

          if (isAccessibilityServiceEnabled) {
            MinuteAccessibilityService.turnScreenOff()
          } else {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(mContext, intent, null)
          }

        }) {
          Text(text = "Put the phone down")
        }
      }
    }
  }
}

fun isAccessibilityServiceEnabled(
  context: Context,
  service: Class<out AccessibilityService?>
): Boolean {
  val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as (AccessibilityManager)
  val enabledServices: List<AccessibilityServiceInfo> =
    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
  for (enabledService in enabledServices) {
    val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
    if (enabledServiceInfo.packageName.equals(context.packageName) && enabledServiceInfo.name.equals(
        service.name
      )
    ) return true
  }
  return false
}