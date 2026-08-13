package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun Modifier.verticalGestureHandler(
  offsetY: Animatable<Float, AnimationVector1D>,
  onActiveGestureChange: (Gesture) -> Unit,
  onEvent: (HomeEvent) -> Unit
): Modifier {
  val coroutineScope = rememberCoroutineScope()
  val hapticFeedback = LocalHapticFeedback.current

  return this.pointerInput(Unit) {
    var gesture = Gesture.NONE
    var thresholdTriggered = false
    val threshold = 50f

    val onDragEnd = {
      thresholdTriggered = false
      coroutineScope.launch {
        offsetY.animateTo(0f, spring(0.55f, 800f))
      }
    }

    detectVerticalDragGestures(
      onDragCancel = { onDragEnd() },
      onDragEnd = {
        onDragEnd()
        onEvent(HomeEvent.HandleGesture(gesture))
      },
    ) { _, dragAmount ->
      val originalY = offsetY.value
      val weight = (abs(originalY) - threshold) / threshold
      val easingFactor = (1 - weight * 0.85f) * 0.10f
      val easedDragAmount = dragAmount * easingFactor

      coroutineScope.launch {
        offsetY.snapTo(originalY + easedDragAmount)
      }

      val isPastThreshold = abs(offsetY.value) > threshold
      if (isPastThreshold && !thresholdTriggered) {
        thresholdTriggered = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
      } else if (!isPastThreshold && thresholdTriggered) {
        thresholdTriggered = false
      }

      val directionDown = offsetY.value > 0
      gesture = when {
        easingFactor >= 0.14 -> Gesture.NONE
        directionDown -> Gesture.DOWN
        thresholdTriggered -> Gesture.UP
        else -> Gesture.NONE
      }

      onActiveGestureChange(gesture)
    }
  }
}