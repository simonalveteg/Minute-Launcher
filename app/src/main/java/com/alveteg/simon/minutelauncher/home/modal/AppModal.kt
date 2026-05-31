package com.alveteg.simon.minutelauncher.home.modal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.delay
import timber.log.Timber


@Composable
fun AppModal(
  appInfo: AppInfo,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onCancel: () -> Unit,
  onChangeTimer: () -> Unit,
  onEditName: () -> Unit
) {
  var enabled by remember { mutableStateOf(false) }
  var timer by remember { mutableIntStateOf(PreferenceRepository.Defaults.TIMER_LENGTH) }
  val usage by remember(appInfo) { mutableStateOf(appInfo.usage) }
  var confirmationText by remember { mutableStateOf("") }
  val animationPeriod = remember(appInfo.timer) {
    (1 - appInfo.timer / PreferenceRepository.Defaults.MAX_TIMER)
      .times(650)
      .plus(150)
  }
  val scale by key(animationPeriod) {
    val infiniteTransition = rememberInfiniteTransition(label = "Put the phone down button")
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.03f,
      animationSpec = infiniteRepeatable(
        animation = tween(animationPeriod, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "Pulsating Button"
    )
  }

  val waitText = stringResource(R.string.label_wait_seconds, timer)
  val openAnywayText = stringResource(R.string.label_open_anyway)

  LaunchedEffect(appInfo) {
    Timber.d("New timer: ${appInfo.timer}, animationPeriod: $animationPeriod, ${PreferenceRepository.Defaults.MAX_TIMER} ")
    timer = appInfo.timer
  }

  LaunchedEffect(key1 = timer) {
    confirmationText = if (timer > 0 && !enabled) {
      waitText
    } else {
      openAnywayText
    }
    
    if (timer > 0 && !enabled) {
      delay(1000L)
      timer -= 1
    } else {
      enabled = true
    }
  }
  AppModalActionBar(
    appInfo = appInfo,
    enabled = enabled,
    onChangeTimer = onChangeTimer,
    onEditName = onEditName,
    onEvent = onEvent
  )
  UsageBarGraph(usageStatistics = usage, modifier = Modifier.padding(vertical = 8.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .graphicsLayer { clip = true }
      .padding(top = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    TextButton(
      modifier = Modifier
        .weight(1f)
        .padding(end = 8.dp),
      onClick = onConfirmation,
      shape = MaterialTheme.shapes.medium,
      enabled = enabled
    ) {
      Box(contentAlignment = Alignment.Center) {
        // Transparent copy for alignment consistency
        Text(
          text = openAnywayText,
          color = Color.Transparent,
          fontFamily = archivoFamily,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = confirmationText,
          fontFamily = archivoFamily,
          fontWeight = FontWeight.Bold
        )
      }
    }
    Button(
      modifier = Modifier
        .wrapContentSize(unbounded = true, align = Alignment.CenterEnd)
        .padding(end = 8.dp)
        .weight(1f)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        },
      onClick = onCancel,
      shape = MaterialTheme.shapes.medium
    ) {
      Text(
        text = stringResource(R.string.label_put_phone_down),
        fontFamily = archivoFamily,
        fontWeight = FontWeight.Bold,
        style = LocalTextStyle.current.copy(textMotion = TextMotion.Animated)
      )
    }
  }
}
