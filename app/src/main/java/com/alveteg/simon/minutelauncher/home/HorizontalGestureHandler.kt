package com.alveteg.simon.minutelauncher.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import kotlin.math.abs
import kotlin.math.max

@Composable
fun Modifier.horizontalGestureHandler(
  activeGesture: Gesture,
  onDragProgressChange: (Float) -> Unit,
  onActiveGestureChange: (Gesture) -> Unit,
  onGestureTriggered: (Boolean) -> Unit,
  onVerticalPositionChange: (Float) -> Unit,
  onEvent: (HomeEvent) -> Unit
): Modifier {
  val hapticFeedback = LocalHapticFeedback.current
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
  val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

  val currentActiveGesture by rememberUpdatedState(activeGesture)

  return this.pointerInput(Unit) {
    var currentZone = GestureZone.NONE
    var startPosition = Offset.Zero
    var hapticTriggered = false
    var maxDistance = 0f

    val baseThreshold = screenWidthPx.div(3)
    val hysteresisBuffer = baseThreshold.div(4)


    val resetDrag = {
      onDragProgressChange(0f)
      maxDistance = 0f
      onGestureTriggered(false)
      hapticTriggered = false
    }

    detectHorizontalDragGestures(
      onDragStart = { offset ->
        startPosition = offset
        currentZone = if (offset.y < screenHeightPx / 2f) GestureZone.UPPER else GestureZone.LOWER
        onVerticalPositionChange(offset.y)
        resetDrag()
      },
      onDragEnd = {
        if (hapticTriggered) {
          onEvent(HomeEvent.HandleGesture(currentActiveGesture))
        }
        resetDrag()
      },
      onHorizontalDrag = { change, _ ->
        onVerticalPositionChange(change.position.y)

        val direction =
          if (change.position.x > startPosition.x) GestureDirection.RIGHT else GestureDirection.LEFT

        val newGesture = when (currentZone) {
          GestureZone.UPPER -> if (direction == GestureDirection.RIGHT) Gesture.TOP_LEFT else Gesture.TOP_RIGHT
          GestureZone.LOWER -> if (direction == GestureDirection.RIGHT) Gesture.BOTTOM_LEFT else Gesture.BOTTOM_RIGHT
          else -> Gesture.NONE
        }
        onActiveGestureChange(newGesture)

        val distance = abs(change.position.x - startPosition.x)
        val newProgress = (distance / baseThreshold).coerceIn(0f, 1f)
        onDragProgressChange(newProgress)

        if (!hapticTriggered) {
          val activationThreshold = if (maxDistance < baseThreshold) {
            baseThreshold
          } else {
            maxDistance + hysteresisBuffer
          }

          if (distance >= activationThreshold) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            hapticTriggered = true
            onGestureTriggered(true)
            maxDistance = distance
          } else {
            if (distance < maxDistance || maxDistance == 0f) {
              maxDistance = distance
            }
          }
        } else {
          maxDistance = max(maxDistance, distance)

          val deactivationThreshold = if (maxDistance > baseThreshold) {
            maxDistance - hysteresisBuffer
          } else {
            baseThreshold
          }

          if (distance < deactivationThreshold || distance < (baseThreshold - hysteresisBuffer)) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            hapticTriggered = false
            onGestureTriggered(false)
            maxDistance = distance
          }
        }
      }
    )
  }
}