package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.settings.PermissionCheckers
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber
import kotlin.math.abs

@Suppress("NAME_SHADOWING")
@Composable
fun FavoriteList(
  screenState: ScreenState,
  favorites: List<AppInfo>,
  onEvent: (Event) -> Unit,
  screenHeight: Float,
  totalUsage: Long,
  offsetY: Animatable<Float, AnimationVector1D>,
  onAppClick: (AppInfo) -> Unit
) {
  val screenHeight by rememberUpdatedState(screenHeight)
  var gesture by remember { mutableStateOf(Gesture.NONE) }
  val coroutineScope = rememberCoroutineScope()
  val onDragEnd = {
    coroutineScope.launch {
      offsetY.animateTo(0f, spring(0.44f, 300f))
    }
  }
  var currentZone by remember { mutableStateOf(GestureZone.NONE) }
  var currentDirection by remember { mutableStateOf(GestureDirection.NONE) }
  val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
  val favoritesAlpha by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.isFavorites()) slowFloatSpec else tween(300)
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        alpha = favoritesAlpha
      }
      .offset { IntOffset(x = 0, y = offsetY.value.toInt()) }
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragCancel = {
            currentZone = GestureZone.NONE
            currentDirection = GestureDirection.NONE
          },
          onDragEnd = {
            gesture = when (currentZone) {
              GestureZone.UPPER -> {
                when (currentDirection) {
                  GestureDirection.RIGHT -> Gesture.TOP_LEFT
                  GestureDirection.LEFT -> Gesture.TOP_RIGHT
                  else -> Gesture.NONE
                }
              }

              GestureZone.LOWER -> {
                when (currentDirection) {
                  GestureDirection.RIGHT -> Gesture.BOTTOM_LEFT
                  GestureDirection.LEFT -> Gesture.BOTTOM_RIGHT
                  else -> Gesture.NONE
                }
              }

              else -> Gesture.NONE
            }
            currentZone = GestureZone.NONE
            currentDirection = GestureDirection.NONE
            onEvent(HomeEvent.HandleGesture(gesture))
          },
        ) { change, dragAmount ->
          currentDirection = if (dragAmount > 0) {
            when (currentDirection) {
              GestureDirection.NONE -> GestureDirection.RIGHT
              GestureDirection.LEFT -> GestureDirection.INVALID
              else -> currentDirection
            }
          } else {
            when (currentDirection) {
              GestureDirection.NONE -> GestureDirection.LEFT
              GestureDirection.RIGHT -> GestureDirection.INVALID
              else -> currentDirection
            }
          }
          Timber.d("Pos: ${change.position.y}, $screenHeight half: ${screenHeight.div(2f)}")
          currentZone = if (change.position.y < screenHeight.div(2f)) {
            when (currentZone) {
              GestureZone.UPPER -> GestureZone.UPPER
              GestureZone.NONE -> GestureZone.UPPER
              else -> GestureZone.INVALID
            }
          } else {
            when (currentZone) {
              GestureZone.LOWER -> GestureZone.LOWER
              GestureZone.NONE -> GestureZone.LOWER
              else -> GestureZone.INVALID
            }
          }
          Timber.d("Direction: $gesture")
        }
      }
      .pointerInput(Unit) {
        detectVerticalDragGestures(
          onDragCancel = { onDragEnd() },
          onDragEnd = {
            onDragEnd()
            onEvent(HomeEvent.HandleGesture(gesture))
          },
        ) { _, dragAmount ->
          val originalY = offsetY.value
          val threshold = 100f
          val weight = (abs(originalY) - threshold) / threshold
          val easingFactor = (1 - weight * 0.85f) * 0.10f
          val easedDragAmount = dragAmount * easingFactor
          coroutineScope.launch {
            offsetY.snapTo(originalY + easedDragAmount)
          }
          gesture = if (easingFactor < 0.14) {
            if (offsetY.value > 0) Gesture.DOWN else Gesture.UP
          } else Gesture.NONE
        }
      },
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = totalUsage.toTimeUsed(),
      color = LocalContentColor.current,
      fontFamily = archivoFamily,
      style = LocalTextStyle.current.copy(
        shadow = Shadow(
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
          blurRadius = 12f
        )
      )
    )

    val listUpdatedChannel = remember { Channel<Unit>() }
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
      Timber.d("Reorder favorite ${from.key} to ${to.key}")
      onEvent(HomeEvent.UpdateFavoriteOrder(from.index, to.index))
      listUpdatedChannel.receive()
      hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(favorites) {
      listUpdatedChannel.trySend(Unit)
    }

    LazyColumn(
      state = listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      userScrollEnabled = false,
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        PermissionCheckers(
          modifier = Modifier.fillMaxWidth(0.8f),
          buttonColors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }
      items(favorites, key = { it.app.packageName }) { appInfo ->
        ReorderableItem(
          state = reorderableLazyListState,
          key = appInfo.app.packageName
        ) { isDragging ->
          FavoriteCard(
            appInfo = appInfo,
            modifier = Modifier.longPressDraggableHandle(
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
    val density = LocalDensity.current
    val bottomHeight = screenHeight.div(6)
    val bottomHeightDp = with(density) { bottomHeight.toDp() }
    Spacer(modifier = Modifier.height(bottomHeightDp))
  }
}