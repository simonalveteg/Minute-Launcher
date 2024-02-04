package com.example.android.minutelauncher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
  var state by remember { mutableStateOf(AppModalState.MAIN) }
  val appUsage = viewModel.getUsageForApp(app).longValue
  val favoriteApps by viewModel.favoriteApps.collectAsState(initial = emptyList())
  val isFavorite = favoriteApps.any { it.app.packageName == app.packageName }
  var enabled by remember { mutableStateOf(false) }
  var timer by remember { mutableIntStateOf(0) }
  var confirmationText by remember { mutableStateOf("") }
  val animationPeriod = when (app.timer) {
    2 -> 700
    5 -> 500
    10 -> 250
    15 -> 150
    else -> Int.MAX_VALUE
  }
  val infiniteTransition = rememberInfiniteTransition(label = "Put the phone down button")
  val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(animationPeriod, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Pulsating Button"
  )

  LaunchedEffect(app) {
    Timber.d("New timer: ${app.timer}")
    timer = app.timer
  }

  LaunchedEffect(key1 = timer) {
    confirmationText = "Wait ${timer}s.."
    if (timer > 0 && !enabled) {
      delay(1000L)
      timer -= 1
    } else {
      confirmationText = "Open Anyway"
      enabled = true
    }
  }

  Surface {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AppModalHeader(
        app = app,
        enabled = enabled,
        isFavorite = isFavorite,
        onStateChanged = { state = state.toggle() },
        onEvent = onEvent
      )
      AnimatedContent(targetState = state, label = "") { state ->
        when (state) {
          AppModalState.MAIN -> AppModalMain(
            app = app,
            appUsage = appUsage,
            enabled = enabled,
            confirmationText = confirmationText,
            onConfirmation = onConfirmation,
            onDismiss = onDismiss,
            scale = scale
          )
          AppModalState.OPTIONS -> AppModalOptions(app = app, onEvent = onEvent)
        }
      }
    }
  }
}

@Composable
private fun AppModalHeader(
  app: App,
  enabled: Boolean,
  isFavorite: Boolean,
  onStateChanged: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val favoriteIcon = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      enabled = enabled,
      onClick = onStateChanged
    ) {
      Icon(imageVector = Icons.Default.Tune, contentDescription = "Modify app settings")
    }
    Text(
      text = app.appTitle, style = MaterialTheme.typography.headlineSmall
    )
    IconButton(
      onClick = {
        onEvent(Event.ToggleFavorite(app))
      }
    ) {
      Icon(imageVector = favoriteIcon, contentDescription = "Favorite")
    }
  }
}

@Composable
private fun AppModalMain(
  app: App,
  appUsage: Long,
  enabled: Boolean,
  confirmationText: String,
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit,
  scale: Float
) {
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
          Text(text = confirmationText)
        }
      }
      Button(
        modifier = Modifier.scale(scale),
        onClick = onDismiss
      ) {
        Text(text = "Put the phone down")
      }
    }
  }
}

@Composable
private fun AppModalOptions(
  app: App,
  onEvent: (Event) -> Unit
) {
  val mContext = LocalContext.current

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
          onEvent(
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

@Composable
private fun AppProperty(
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

private enum class AppModalState {
  MAIN, OPTIONS;

  fun toggle(): AppModalState {
    return when (this) {
      MAIN -> OPTIONS
      OPTIONS -> MAIN
    }
  }
}