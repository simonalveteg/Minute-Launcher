package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.FavoriteAppInfo
import com.alveteg.simon.minutelauncher.data.toAppInfo
import com.alveteg.simon.minutelauncher.reorderableList.ReorderableItem
import com.alveteg.simon.minutelauncher.reorderableList.detectReorder
import com.alveteg.simon.minutelauncher.reorderableList.rememberReorderableLazyListState
import com.alveteg.simon.minutelauncher.reorderableList.reorderable
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.GestureDirection
import com.alveteg.simon.minutelauncher.utilities.GestureZone
import com.alveteg.simon.minutelauncher.utilities.thenIf
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConstraintLayoutScope.FavoriteList(
  screenState: ScreenState,
  favorites: List<FavoriteAppInfo>,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onEvent: (Event) -> Unit,
  screenHeight: Float,
  totalUsage: Long,
  onAppClick: (AppInfo) -> Unit
) {
  var gesture by remember { mutableStateOf(Gesture.NONE) }
  val offsetY = remember { Animatable(0f) }
  val coroutineScope = rememberCoroutineScope()
  val onDragEnd = {
    coroutineScope.launch {
      offsetY.animateTo(0f, spring(0.5f, 300f))
    }
  }
  var currentZone by remember { mutableStateOf(GestureZone.NONE) }
  var currentDirection by remember { mutableStateOf(GestureDirection.NONE) }
  val fastFloatSpec: AnimationSpec<Float> = tween(durationMillis = 500)
  val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
  val favoritesAlpha by animateFloatAsState(
    targetValue = if (screenState.hasSearch()) 0f else 1f,
    label = "",
    animationSpec = if (screenState.hasSearch()) fastFloatSpec else slowFloatSpec
  )
  val usageAlpha by animateFloatAsState(
    targetValue = if (screenState.isFavorites()) 1f else 0f,
    label = "",
    animationSpec = if (!screenState.isFavorites()) fastFloatSpec else tween(
      durationMillis = 500, delayMillis = 600
    )
  )

  Column(modifier = Modifier
    .constrainAs(constraintReference) { constraints() }
    .fillMaxSize()
    .graphicsLayer {
      alpha = favoritesAlpha
    }
    .combinedClickable(onLongClick = {
      onEvent(Event.ChangeScreenState(screenState.toggleModify()))
    }) {}
    .offset { IntOffset(x = 0, y = offsetY.value.roundToInt()) }
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
          onEvent(Event.HandleGesture(gesture))
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
        currentZone = if (change.position.y < screenHeight.div(2)) {
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
          onEvent(Event.HandleGesture(gesture))
        },
      ) { _, dragAmount ->
        val originalY = offsetY.value
        val threshold = 100f
        val weight = (abs(originalY) - threshold) / threshold
        val easingFactor = (1 - weight * 0.75f) * 0.25f
        val easedDragAmount = dragAmount * easingFactor
        coroutineScope.launch {
          offsetY.snapTo(originalY + easedDragAmount)
        }
        gesture = if (easingFactor < 0.18) {
          if (offsetY.value > 0) Gesture.DOWN else Gesture.UP
        } else Gesture.NONE
      }
    },
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = totalUsage.toTimeUsed(), color = LocalContentColor.current.copy(alpha = usageAlpha)
    )
    val data = remember { mutableStateOf(favorites) }
    LaunchedEffect(favorites) {
      if (favorites.size != data.value.size) {
        data.value = favorites
      }
    }
    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
      data.value = data.value.toMutableList().apply {
        add(to.index, removeAt(from.index))
      }
      onEvent(Event.UpdateFavoriteOrder(data.value))
    })
    LazyColumn(state = reorderableState.listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      userScrollEnabled = false,
      modifier = Modifier
        .fillMaxWidth()
        .thenIf(screenState.isModify()) { detectReorder(reorderableState) }
        .thenIf(screenState.isModify()) { reorderable(reorderableState) }
    ) {
      items(data.value, { it.favoriteApp.app.id }) { favoriteAppInfo ->
        ReorderableItem(reorderableState, key = favoriteAppInfo.favoriteApp.app.id) { isDragged ->
          AppCard(
            appTitle = favoriteAppInfo.favoriteApp.app.appTitle,
            appUsage = favoriteAppInfo.usage,
            editState = screenState.isModify(),
            isDragged = isDragged
          ) {
            if (screenState.isFavorites()) onAppClick(favoriteAppInfo.toAppInfo())
          }
        }
      }
    }
    val density = LocalDensity.current
    val bottomHeight = screenHeight.div(6)
    val bottomHeightDp = with(density) { bottomHeight.toDp() }
    Spacer(modifier = Modifier.height(bottomHeightDp))
  }
}