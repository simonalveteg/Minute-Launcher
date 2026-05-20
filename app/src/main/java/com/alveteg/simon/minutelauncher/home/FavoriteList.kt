package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.settings.PermissionCheckers
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableColumn
import timber.log.Timber
import kotlin.math.abs

@Suppress("NAME_SHADOWING")
@Composable
fun FavoriteList(
  screenState: ScreenState,
  favorites: List<AppInfo>,
  onEvent: (Event) -> Unit,
  screenHeight: Float,
  screenWidth: Float,
  totalUsage: Long,
  offsetY: Animatable<Float, AnimationVector1D>,
  showPermissionPrompts: Boolean,
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
  var activeGesture by remember { mutableStateOf(Gesture.NONE) }
  val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
  val favoritesAlpha by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.isFavorites()) slowFloatSpec else tween(300)
  )

  val hapticFeedback = LocalHapticFeedback.current


  var dragProgress by remember { mutableStateOf(0f) }

  var isTriggered by remember { mutableStateOf(false) }
  val popScale by animateFloatAsState(
    targetValue = if (isTriggered) 1.5f else 0.8f,
    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
    label = "PopAnimation"
  )

  val indicatorColor by animateColorAsState(
    targetValue = if (isTriggered) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), label = "IndicatorColor"
  )


  Box {
    val animatedWidth by animateFloatAsState(
      targetValue = dragProgress, label = "GestureIndicator"
    )

    if (animatedWidth > 0.05f) {

      val alignment = when (activeGesture) {
        Gesture.TOP_LEFT -> Alignment.TopStart
        Gesture.TOP_RIGHT -> Alignment.TopEnd
        Gesture.BOTTOM_LEFT -> Alignment.BottomStart
        Gesture.BOTTOM_RIGHT -> Alignment.BottomEnd
        else -> Alignment.Center
      }

      val shape = if (activeGesture.isLeft()) {
        RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
      } else {
        RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
      }

      Box(
        modifier = Modifier
          .fillMaxHeight(0.5f)
          .width((24.dp * animatedWidth) * popScale)
          .align(alignment)
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight(0.6f)
            .fillMaxWidth()
            .align(Alignment.Center)
            .background(
              color = indicatorColor, shape = shape
            )
        )
      }
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .offset { IntOffset(x = 0, y = offsetY.value.toInt()) }
        .graphicsLayer(alpha = favoritesAlpha)
        .pointerInput(Unit) {
          var currentZone = GestureZone.NONE
          var startPosition = Offset.Zero
          val triggerThreshold = screenWidth.div(3)

          detectHorizontalDragGestures(
            onDragStart = { offset ->
              startPosition = offset
              dragProgress = 0f
              currentZone =
                if (offset.y < screenHeight / 2f) GestureZone.UPPER else GestureZone.LOWER
            },
            onDragCancel = {
              dragProgress = 0f
              isTriggered = false
              activeGesture = Gesture.NONE
            },
            onDragEnd = {
              if (dragProgress >= 1f) {
                onEvent(HomeEvent.HandleGesture(activeGesture))
              }

              dragProgress = 0f
              isTriggered = false
            },
            onHorizontalDrag = { change, _ ->

              val direction =
                if (change.position.x > startPosition.x) GestureDirection.RIGHT else GestureDirection.LEFT
              activeGesture = when (currentZone) {
                GestureZone.UPPER -> if (direction == GestureDirection.RIGHT) Gesture.TOP_LEFT else Gesture.TOP_RIGHT
                GestureZone.LOWER -> if (direction == GestureDirection.RIGHT) Gesture.BOTTOM_LEFT else Gesture.BOTTOM_RIGHT
                else -> Gesture.NONE
              }

              val distance = abs(change.position.x - startPosition.x)
              dragProgress = (distance / triggerThreshold).coerceIn(0f, 1f)

              val reachedThreshold = dragProgress >= 1f

              if (reachedThreshold && !isTriggered) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
              }

              isTriggered = reachedThreshold
            }
          )
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
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Bottom,
    ) {
      PermissionCheckers(
        enabled = showPermissionPrompts,
        showDismissButton = true,
        onDismiss = { onEvent(HomeEvent.HidePermissionPrompts) },
        modifier = Modifier.fillMaxWidth(0.8f),
        buttonColors = ButtonDefaults.outlinedButtonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
      val density = LocalDensity.current
      val bottomHeight = screenHeight.div(6)
      val bottomHeightDp = with(density) { bottomHeight.toDp() }
      Spacer(modifier = Modifier.height(bottomHeightDp))
    }
  }
}
