package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.settings.PermissionCheckers
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableColumn
import timber.log.Timber
import kotlin.time.Duration

@Composable
fun FavoriteList(
  screenState: ScreenState,
  favorites: List<AppInfo>,
  onEvent: (Event) -> Unit,
  totalUsage: Duration?,
  offsetY: Animatable<Float, AnimationVector1D>,
  showDefaultHomePrompt: Boolean,
  showAdminAccessPrompt: Boolean,
  showUsageAccessPrompt: Boolean,
  onAppClick: (AppInfo) -> Unit
) {
  val hapticFeedback = LocalHapticFeedback.current

  var dragProgress by remember { mutableFloatStateOf(0f) }
  var activeGesture by remember { mutableStateOf(Gesture.NONE) }
  var isTriggered by remember { mutableStateOf(false) }
  var touchPosition by remember { mutableStateOf(Offset.Zero) }

  val favoritesAlpha by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.isFavorites()) tween(
      delayMillis = 100, durationMillis = 600
    ) else tween(100)
  )
  val favoritesProgress by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.isFavorites()) tween(delayMillis = 100, durationMillis = 300) else tween(100)
  )
  val favoritesScaleX by remember { derivedStateOf { lerp(0.97f, 1f, favoritesProgress) } }
  val favoritesScaleY by remember { derivedStateOf { lerp(0.9f, 1f, favoritesProgress) } }

  val bottomHeightDp = LocalWindowInfo.current.containerDpSize.height.div(6)

  Box {
    GestureIndicator(
      dragProgress = dragProgress,
      isTriggered = isTriggered,
      activeGesture = activeGesture,
      fingerPosition = touchPosition
    )
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .offset { IntOffset(x = 0, y = offsetY.value.toInt()) }
        .graphicsLayer(alpha = favoritesAlpha)
        .horizontalGestureHandler(
          activeGesture = activeGesture,
          onDragProgressChange = { dragProgress = it },
          onActiveGestureChange = { activeGesture = it },
          onGestureTriggered = { isTriggered = it },
          onVerticalPositionChange = { touchPosition = it },
          onEvent = { onEvent(it) })
        .verticalGestureHandler(
          offsetY = offsetY,
          onActiveGestureChange = { activeGesture = it },
          onEvent = { onEvent(it) }),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Bottom,
    ) {
      PermissionCheckers(
        showDismissButton = true,
        showAdminAccessPrompt = showAdminAccessPrompt,
        showUsageAccessPrompt = showUsageAccessPrompt,
        showDefaultHomePrompt = showDefaultHomePrompt,
        modifier = Modifier.fillMaxWidth(0.8f),
        buttonColors = ButtonDefaults.outlinedButtonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onEvent = onEvent
      )
      Text(
        text = totalUsage.toTimeUsed(),
        color = LocalContentColor.current,
        fontFamily = archivoFamily,
        style = LocalTextStyle.current.copy(
          shadow = Shadow(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f), blurRadius = 12f
          )
        )
      )
      ReorderableColumn(
        list = favorites,
        onSettle = { from, to ->
          Timber.d("Reorder favorite $from to $to")
          val itemsAbove = 0
          val fromIndex = from - itemsAbove
          val toIndex = to - itemsAbove
          onEvent(HomeEvent.UpdateFavoriteOrder(fromIndex, toIndex))
          hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxWidth()
      ) { _, appInfo, _ ->
        key(appInfo.app.packageName) {
          ReorderableItem {
            FavoriteCard(
              appInfo = appInfo, modifier = Modifier
                .longPressDraggableHandle(
                  onDragStarted = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                  },
                  onDragStopped = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                  },
                )
                .graphicsLayer(scaleX = favoritesScaleX, scaleY = favoritesScaleY)
            ) { onAppClick(appInfo) }
          }
        }
      }
      Spacer(modifier = Modifier.height(bottomHeightDp))
    }
  }
}
