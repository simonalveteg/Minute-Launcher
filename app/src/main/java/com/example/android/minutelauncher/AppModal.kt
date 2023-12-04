package com.example.android.minutelauncher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App
import kotlinx.coroutines.delay
import timber.log.Timber


@Composable
fun AppModal(
  app: App,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit,
  viewModel: LauncherViewModel = hiltViewModel(),
) {
  val mContext = LocalContext.current
  val appUsage = viewModel.getUsageForApp(app).longValue
  val favoriteApps by viewModel.favoriteApps.collectAsState(initial = emptyList())
  val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
  val isFavorite = favoriteApps.any { it.app.packageName == app.packageName }
  val isGesture = gestureApps.any { it.value.packageName == app.packageName }
  val favoriteIcon = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder
  var enabled by remember { mutableStateOf(false) }
  var timer by remember { mutableIntStateOf(0) }
  var openText by remember { mutableStateOf("") }
  val animationPeriod = 350
  val scale = remember { Animatable(1f) }

  var modify by remember { mutableStateOf(false) }

  LaunchedEffect(app) {
    Timber.d("New timer: ${app.timer}")
    timer = app.timer
  }

  LaunchedEffect(key1 = timer) {
    openText = "Wait ${timer}s.."
    if (timer > 0 && !enabled) {
      delay(1000L)
      timer -= 1
    } else {
      openText = "Open Anyway"
      enabled = true
    }
  }

  LaunchedEffect(key1 = enabled) {
    if (!enabled) {
      delay(500L)
      while (true) {
        delay(animationPeriod.toLong())
        scale.animateTo(
          targetValue = 1.03f,
          animationSpec = tween(animationPeriod)
        )
        delay(animationPeriod.toLong())
        scale.animateTo(
          targetValue = 1f,
          animationSpec = tween(animationPeriod)
        )
      }
    } else {
      scale.animateTo(
        targetValue = 1f,
        animationSpec = tween(animationPeriod)
      )
    }
  }

  Surface {
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
        IconButton(
          enabled = enabled,
          onClick = {
            modify = !modify
          }
        ) {
          Icon(imageVector = Icons.Default.Tune, contentDescription = "Modify app settings")
        }
        Text(
          text = app.appTitle, style = MaterialTheme.typography.headlineSmall
        )
        IconButton(
          enabled = !isGesture,
          onClick = {
            onEvent(Event.ToggleFavorite(app))
          }
        ) {
          Icon(imageVector = favoriteIcon, contentDescription = "Favorite")
        }
      }
      AnimatedVisibility(visible = !modify) {
        Column {
          Text(
            text = "${app.appTitle} used for ${appUsage.toTimeUsed(false)}",
            modifier = Modifier.padding(64.dp)
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 22.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
          ) {
            TextButton(onClick = onConfirmation, enabled = enabled) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = "Open Anyway", color = Color.Transparent) // for alignment consistency
                Text(text = openText)
              }
            }
            Button(
              modifier = Modifier.scale(scale.value),
              onClick = onDismiss
            ) {
              Text(text = "Put the phone down")
            }
          }
        }
      }
      AnimatedVisibility(visible = modify) {
        Column(
          modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          AppProperty(label = "App timer") {
            SegmentedControl(
              items = setOf(0, 2, 5, 10, 15),
              selectedItem = app.timer,
              onItemSelection = {
                viewModel.onEvent(
                  Event.UpdateApp(
                    app.copy(timer = it)
                  )
                )
              }
            )
          }
          TextButton(onClick = {
            val intent = Intent().apply {
              action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
              data = Uri.fromParts("package", app.packageName, null)
            }
            startActivity(mContext, intent, null)
          }) {
            Text(text = "Open App Info")
          }
        }
      }
    }
  }
}

@Composable
fun AppProperty(
  label: String,
  content: @Composable ColumnScope.() -> Unit
) {
  Column {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelMedium
    )
    content()
  }
}