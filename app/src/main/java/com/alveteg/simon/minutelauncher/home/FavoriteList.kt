package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.settings.PermissionCheckers
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import sh.calvin.reorderable.ReorderableColumn
import timber.log.Timber

@Composable
fun FavoriteList(
  screenState: ScreenState,
  favorites: List<AppInfo>,
  onEvent: (Event) -> Unit,
  totalUsage: Long,
  offsetY: Animatable<Float, AnimationVector1D>,
  showDefaultHomePrompt: Boolean,
  showAdminAccessPrompt: Boolean,
  showUsageAccessPrompt: Boolean,
  onAppClick: (AppInfo) -> Unit
) {
  val hapticFeedback = LocalHapticFeedback.current

  var dragProgress by remember { mutableStateOf(0f) }
  var activeGesture by remember { mutableStateOf(Gesture.NONE) }
  var isTriggered by remember { mutableStateOf(false) }
  var verticalTouchPosition by remember { mutableStateOf(0f) }

  val favoritesAlpha by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.isFavorites()) tween(durationMillis = 1000) else tween(300)
  )

  val configuration = LocalConfiguration.current
  val bottomHeight = configuration.screenHeightDp.div(6)
  val bottomHeightDp = bottomHeight.dp

  Box {
    GestureIndicator(
      dragProgress = dragProgress,
      isTriggered = isTriggered,
      activeGesture = activeGesture,
      verticalPosition = verticalTouchPosition
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
          onVerticalPositionChange = { verticalTouchPosition = it },
          onEvent = { onEvent(it) }
        )
        .verticalGestureHandler(
          offsetY = offsetY,
          onActiveGestureChange = { activeGesture = it },
          onEvent = { onEvent(it) }
        ),
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
              appInfo = appInfo, modifier = Modifier.longPressDraggableHandle(
                onDragStarted = {
                  hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                },
                onDragStopped = {
                  hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                },
              )
            ) { onAppClick(appInfo) }
          }
        }
      }
      Spacer(modifier = Modifier.height(bottomHeightDp))
    }
  }
}
