package com.alveteg.simon.minutelauncher.settings

import android.app.admin.DevicePolicyManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alveteg.simon.minutelauncher.MinuteDeviceAdminReceiver
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.home.LocalSystemPermissions
import com.alveteg.simon.minutelauncher.settings.components.ButtonInput

@Composable
fun PermissionCheckers(
  modifier: Modifier = Modifier,
  showDefaultHomePrompt: Boolean = true,
  showAdminAccessPrompt: Boolean = true,
  showUsageAccessPrompt: Boolean = true,
  showDismissButton: Boolean = true,
  buttonColors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
  onEvent: (HomeEvent) -> Unit,
) {

  val context = LocalContext.current
  val permissions = LocalSystemPermissions.current
  val roleRequestLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { }
  val isAdminActive = permissions.isDeviceAdmin
  val hasUsageAccess = permissions.isUsageGranted
  val isDefaultLauncher = permissions.isDefaultLauncher

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (!isDefaultLauncher && showDefaultHomePrompt) {
      ButtonInput(
        label = stringResource(R.string.label_set_default_launcher),
        colors = buttonColors,
        imageVector = Icons.Default.Home,
        onClick = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager

            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
              !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
              val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
              roleRequestLauncher.launch(intent)
            } else {
              // Already default, or role not available - open settings as fallback
              val intent = Intent(Settings.ACTION_HOME_SETTINGS)
              context.startActivity(intent)
            }
          } else {
            // Fallback for older Android versions
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            context.startActivity(intent)
          }
        },
        showDismiss = showDismissButton,
        onDismiss = { onEvent(HomeEvent.HideDefaultAppPrompt) }
      )
    }
    if (!isAdminActive && showAdminAccessPrompt) {
      val adminExplanationString = stringResource(R.string.admin_access_explanation)
      ButtonInput(
        label = stringResource(R.string.label_grant_admin_access),
        imageVector = Icons.Default.AdminPanelSettings,
        description = stringResource(R.string.description_admin_access),
        colors = buttonColors,
        onClick = {
          val componentName = ComponentName(context, MinuteDeviceAdminReceiver::class.java)
          val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
              DevicePolicyManager.EXTRA_ADD_EXPLANATION,
              adminExplanationString
            )
          }
          context.startActivity(intent)
        },
        showDismiss = showDismissButton,
        onDismiss = { onEvent(HomeEvent.HideAdminAccessPrompt) }
      )
    }
    if (!hasUsageAccess && showUsageAccessPrompt) {
      ButtonInput(
        label = stringResource(R.string.label_grant_usage_access),
        description = stringResource(R.string.description_usage_access),
        imageVector = Icons.Default.QueryStats,
        colors = buttonColors,
        onClick = {
          val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
          context.startActivity(intent)
        },
        showDismiss = showDismissButton,
        onDismiss = { onEvent(HomeEvent.HideUsageAccessPrompt) }
      )
    }
  }
}
