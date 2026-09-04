package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.ScaleIndicationNodeFactory
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteCard(
  appInfo: AppInfo,
  modifier: Modifier = Modifier,
  color: Color = Color.Transparent,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val viewConfiguration = LocalViewConfiguration.current
  val scope = rememberCoroutineScope()
  val appTitle = appInfo.app.displayTitle ?: appInfo.app.appTitle
  val appUsage by remember(appInfo) {
    derivedStateOf { appInfo.usage.firstOrNull { it.usageDate == LocalDate.now() }?.usageDuration }
  }

  Surface(
    shape = MaterialTheme.shapes.large,
    color = color,
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize()
      .indication(interactionSource, ScaleIndicationNodeFactory)
      .pointerInput(onClick) {
        val touchSlop = viewConfiguration.touchSlop
        val tapTimeout = 100L

        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)

          var pressJob: Job? = scope.launch {
            delay(tapTimeout)
            if (isActive) {
              interactionSource.emit(PressInteraction.Press(down.position))
            }
          }
          var press: PressInteraction.Press? = null
          val trackedPress = PressInteraction.Press(down.position)

          var pastSlop = false
          var released = false

          while (!pastSlop && !released) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break

            if (!change.pressed) {
              released = true
              break
            }

            val movedDistance = (change.position - down.position).getDistance()
            if (movedDistance > touchSlop) {
              pastSlop = true
            }
          }

          if (pastSlop) {
            pressJob?.cancel()
            scope.launch {
              interactionSource.emit(PressInteraction.Cancel(trackedPress))
            }
            return@awaitEachGesture
          }

          pressJob?.cancel()
          scope.launch {
            interactionSource.emit(trackedPress)
            interactionSource.emit(PressInteraction.Release(trackedPress))
          }
          onClick()
        }
      }
      .padding(vertical = 2.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = appTitle,
        fontFamily = archivoBlackFamily,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(
          shadow = Shadow(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            blurRadius = 12f
          )
        ),
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
      )
      Text(
        text = appUsage.toTimeUsed(),
        fontFamily = archivoFamily,
        color = MaterialTheme.colorScheme.primary,
        style = LocalTextStyle.current.copy(
          shadow = Shadow(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            blurRadius = 12f
          )
        )
      )
    }
  }
}