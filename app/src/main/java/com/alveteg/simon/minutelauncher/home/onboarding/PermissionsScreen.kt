package com.alveteg.simon.minutelauncher.home.onboarding

import android.app.admin.DevicePolicyManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alveteg.simon.minutelauncher.MinuteDeviceAdminReceiver
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.home.LocalSystemPermissions
import com.alveteg.simon.minutelauncher.home.isDefaultLauncher
import com.alveteg.simon.minutelauncher.home.isDeviceAdmin
import com.alveteg.simon.minutelauncher.home.isUsageAccessGranted

@OptIn(
  ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SharedTransitionScope.PermissionsScreen(
  animatedVisibilityScope: AnimatedVisibilityScope,
  onFinish: () -> Unit,
  onEvent: (HomeEvent) -> Unit
) {

  val context = LocalContext.current
  val adminExplanationString = stringResource(R.string.admin_access_explanation)
  val roleRequestLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { }

  val permissions = LocalSystemPermissions.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
      .padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.End
  ) {
    Text(
      text = "Minute Launcher",
      style = MaterialTheme.typography.displayLargeEmphasized,
      modifier = Modifier.padding(bottom = 36.dp)
    )
    Surface(
      shape = MaterialTheme.shapes.extraLarge
    ) {
      Column(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
      ) {
        PermissionPrompt(
          title = stringResource(R.string.label_set_default_launcher),
          enabled = !permissions.isDefaultLauncher,
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
          }
        )
        PermissionPrompt(
          title = stringResource(R.string.label_grant_usage_access),
          description = stringResource(R.string.description_usage_access),
          enabled = !permissions.isUsageGranted,
          onClick = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
          },
        )
        PermissionPrompt(
          title = stringResource(R.string.label_grant_admin_access),
          description = stringResource(R.string.description_admin_access),
          enabled = !permissions.isDeviceAdmin,
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
        )
      }
    }
    FilledTonalButton(
      onClick = onFinish,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ),
      contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        contentDescription = null,
        modifier = Modifier.size(ButtonDefaults.IconSize)
      )
      Spacer(Modifier.size(ButtonDefaults.IconSpacing))
      Text(
        text = "Continue",
        style = MaterialTheme.typography.bodyMediumEmphasized
      )
    }
  }
}
