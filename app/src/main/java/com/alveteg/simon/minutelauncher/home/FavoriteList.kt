package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.FavoriteAppInfo
import com.alveteg.simon.minutelauncher.data.toAppInfo
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@Suppress("NAME_SHADOWING")
@Composable
fun FavoriteList(
  screenState: ScreenState,
  favorites: List<FavoriteAppInfo>,
  onEvent: (Event) -> Unit,
  screenHeight: Float,
  totalUsage: Long,
  offsetY: Animatable<Float, AnimationVector1D>,
  onAppClick: (AppInfo) -> Unit
) {
  val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
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
  val favoritesAlpha = remember { Animatable(0f) }

  LaunchedEffect(key1 = lifecycleState) {
    when (lifecycleState) {
      Lifecycle.State.RESUMED -> {
        if (screenState.isFavorites()) {
          coroutineScope.launch {
            offsetY.snapTo(-60f)
            offsetY.animateTo(0f, spring(0.44f, 300f))
          }
          coroutineScope.launch {
            favoritesAlpha.snapTo(0f)
            favoritesAlpha.animateTo(1f)
          }
        }
      }

      else -> Unit
    }
  }

  LaunchedEffect(screenState) {
    when (screenState) {
      ScreenState.FAVORITES -> {
        coroutineScope.launch {
          favoritesAlpha.animateTo(1f, slowFloatSpec)
        }
      }

      ScreenState.DASHBOARD -> {
        coroutineScope.launch {
          favoritesAlpha.animateTo(0f, tween(300))
        }
      }
    }
  }

  Column(modifier = Modifier
    .fillMaxSize()
    .graphicsLayer {
      alpha = favoritesAlpha.value
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
                GestureDirection.RIGHT -> Gesture.UPPER_RIGHT
                GestureDirection.LEFT -> Gesture.UPPER_LEFT
                else -> Gesture.NONE
              }
            }

            GestureZone.LOWER -> {
              when (currentDirection) {
                GestureDirection.RIGHT -> Gesture.LOWER_RIGHT
                GestureDirection.LEFT -> Gesture.LOWER_LEFT
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
    val listState = rememberLazyListState()
    Spacer(modifier = Modifier.weight(4f))
    LazyColumn(
      state = listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      userScrollEnabled = false,
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      item {
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
      }
      items(favorites) { favoriteAppInfo ->
        FavoriteCard(favoriteAppInfo.toAppInfo()) { onAppClick(favoriteAppInfo.toAppInfo()) }
      }
    }
    Spacer(modifier = Modifier.weight(1.2f))
  }
}