package com.alveteg.simon.minutelauncher.home.modal

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.delay
import timber.log.Timber


@Composable
fun AppModal(
  appInfo: AppInfo,
  timerMapping: List<AccessTimerMapping>,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onCancel: () -> Unit,
  onChangeTimer: () -> Unit
) {
  var enabled by remember { mutableStateOf(false) }
  var timer by remember { mutableIntStateOf(0) }
  val usage by remember { mutableStateOf(appInfo.usage) }
  var confirmationText by remember { mutableStateOf("") }
  val animationPeriod = when (appInfo.app.timer) {
    AccessTimer.SHORT -> 700
    AccessTimer.MEDIUM -> 500
    AccessTimer.LONG -> 250
    AccessTimer.ETERNITY -> 150
    else -> Int.MAX_VALUE
  }
  val timerValue = timerMapping.first { it.enum == appInfo.app.timer }.integerValue
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

  LaunchedEffect(appInfo) {
    Timber.d("New timer: ${appInfo.app.timer}")
    timer = timerValue
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
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .graphicsLayer { clip = true }
      .padding(horizontal = 16.dp)
      .padding(bottom = 8.dp)
      .navigationBarsPadding()
      .animateContentSize()
  ) {
    Text(
      text = appInfo.app.appTitle,
      style = MaterialTheme.typography.headlineSmall,
      fontFamily = archivoBlackFamily,
      modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
    AppModalActionBar(
      appInfo = appInfo,
      enabled = enabled,
      onChangeTimer = onChangeTimer,
      onEvent = onEvent
    )
    UsageBarGraph(usage)
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
            text = "Open Anyway",
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
          text = "Put the phone down",
          fontFamily = archivoFamily,
          fontWeight = FontWeight.Bold,
          style = LocalTextStyle.current.copy(textMotion = TextMotion.Animated)
        )
      }
    }
  }
}