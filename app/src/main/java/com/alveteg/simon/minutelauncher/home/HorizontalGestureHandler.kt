package com.alveteg.simon.minutelauncher.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.times
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import kotlin.math.abs

@Composable
fun Modifier.horizontalGestureHandler(
  screenWidth: Float,
  screenHeight: Float,
  activeGesture: Gesture,
  dragProgress: Float,
  onDragProgressChange: (Float) -> Unit,
  onActiveGestureChange: (Gesture) -> Unit,
  onGestureTriggered: (Boolean) -> Unit,
  onEvent: (HomeEvent) -> Unit
): Modifier {
  val hapticFeedback = LocalHapticFeedback.current
  val currentActiveGesture by rememberUpdatedState(activeGesture)
  val currentDragProgress by rememberUpdatedState(dragProgress)

  return this.pointerInput(Unit) {
    var currentZone = GestureZone.NONE
    var startPosition = Offset.Zero
    var hapticTriggered = false
    val triggerThreshold = screenWidth.div(3)
    var maxDistance = 0f

    val resetDrag = {
      onDragProgressChange(0f)
      maxDistance = 0f
      onGestureTriggered(false)
      hapticTriggered = false
    }

    detectHorizontalDragGestures(
      onDragStart = { offset ->
        startPosition = offset
        currentZone = if (offset.y < screenHeight / 2f) GestureZone.UPPER else GestureZone.LOWER
        resetDrag()
      },
      onDragEnd = {
        if (currentDragProgress >= 1f) {
          onEvent(HomeEvent.HandleGesture(currentActiveGesture))
        }
        resetDrag()
      },
      onHorizontalDrag = { change, _ ->
        val direction =
          if (change.position.x > startPosition.x) GestureDirection.RIGHT else GestureDirection.LEFT

        val newGesture = when (currentZone) {
          GestureZone.UPPER -> if (direction == GestureDirection.RIGHT) Gesture.TOP_LEFT else Gesture.TOP_RIGHT
          GestureZone.LOWER -> if (direction == GestureDirection.RIGHT) Gesture.BOTTOM_LEFT else Gesture.BOTTOM_RIGHT
          else -> Gesture.NONE
        }
        onActiveGestureChange(newGesture)

        val distance = abs(change.position.x - startPosition.x)
        val newProgress = (distance / triggerThreshold).coerceIn(0f, 1f)
        onDragProgressChange(newProgress)

        val reachedThreshold = newProgress >= 1f

        if (reachedThreshold && !hapticTriggered) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
          hapticTriggered = true
        } else if (!reachedThreshold) {
          hapticTriggered = false
        }

        onGestureTriggered(reachedThreshold)
      }
    )
  }
}