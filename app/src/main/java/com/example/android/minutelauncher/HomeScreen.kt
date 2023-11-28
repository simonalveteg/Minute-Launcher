package com.example.android.minutelauncher

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.reorderableList.ReorderableItem
import com.example.android.minutelauncher.reorderableList.detectReorder
import com.example.android.minutelauncher.reorderableList.rememberReorderableLazyListState
import com.example.android.minutelauncher.reorderableList.reorderable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val screenState by viewModel.screenState.collectAsState()
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val appListState = rememberLazyListState()
  val appSelectorVisible = remember { mutableStateOf(false) }
  val selectedDirection = remember { mutableStateOf<Gesture?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val currentAppModal by viewModel.currentModal.collectAsState()
  val searchText by viewModel.searchTerm.collectAsState()
  val apps by viewModel.filteredApps.collectAsState(initial = emptyList())
  var screenHeight by remember { mutableFloatStateOf(0f) }
  var searchHeight by remember { mutableFloatStateOf(0f) }

  val backgroundColor by animateColorAsState(
    targetValue = when (screenState) {
      ScreenState.FAVORITES -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      ScreenState.MODIFY -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
      ScreenState.APPS -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    },
    label = ""
  )
  val dialogSheetScaffoldState =
    rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      confirmValueChange = {
        if (it == ModalBottomSheetValue.Hidden) viewModel.onEvent(Event.ClearModal)
        true
      })

  LaunchedEffect(key1 = true) {
    Timber.d("launched effect")
    viewModel.uiEvent.collect { event ->
      Timber.d("event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.VibrateLongPress -> {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.ExpandNotifications -> {
          setExpandNotificationDrawer(mContext, true)
        }
      }
    }
  }

  BackHandler(true) {
    viewModel.onEvent(Event.ChangeScreenState(ScreenState.FAVORITES))
  }

  LaunchedEffect(key1 = currentAppModal) {
    if (currentAppModal != null) {
      launch { dialogSheetScaffoldState.show() }
      keyboardController?.hide()
      try {
        focusRequester.freeFocus()
      } catch (e: Exception) {
        Timber.d("Free focus on composable failed: not in composition?")
      }
    } else {
      launch { dialogSheetScaffoldState.hide() }
    }
  }

  LaunchedEffect(key1 = screenState) {
    when (screenState) {
      ScreenState.FAVORITES -> {
        delay(500)
        appListState.scrollToItem(0)
      }

      else -> viewModel.onEvent(Event.UpdateSearch(""))
    }
  }

  ModalBottomSheetLayout(
    sheetState = dialogSheetScaffoldState,
    sheetBackgroundColor = MaterialTheme.colorScheme.background,
    sheetShape = MaterialTheme.shapes.large.copy(
      bottomStart = CornerSize(0),
      bottomEnd = CornerSize(0)
    ),
    sheetContent = {
      Spacer(modifier = Modifier.height(4.dp))
      if (currentAppModal != null) {
        val app = currentAppModal!!
        BackHandler(true) { viewModel.onEvent(Event.ClearModal) }
        AppModal(
          app = app,
          onEvent = viewModel::onEvent,
          onConfirmation = {
            viewModel.onEvent(Event.LaunchActivity(app))
            viewModel.onEvent(Event.ClearModal)
          },
          onDismiss = {
            val isAccessibilityServiceEnabled =
              isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)
            if (isAccessibilityServiceEnabled) {
              MinuteAccessibilityService.turnScreenOff()
              viewModel.onEvent(Event.ClearModal)
            } else {
              val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
              ContextCompat.startActivity(mContext, intent, null)
            }
          }
        )
      }
    }
  ) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
      Surface(color = backgroundColor) {
        ConstraintLayout(
          modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
              screenHeight = it.size.height.toFloat()
            }
        ) {
          val (favList, appList, searchBar, topLeft, topRight, bottomLeft, bottomRight) = createRefs()
          val sideWidth by animateFloatAsState(
            targetValue =
            when (screenState) {
              ScreenState.MODIFY -> 60f
              else -> 0f
            }, label = "",
            animationSpec = tween(200, 0, EaseInOutQuad)
          )
          val bottomHeight = screenHeight.div(6)
          val totalUsage by viewModel.getTotalUsage()
          val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
          val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
          var gesture by remember { mutableStateOf(Gesture.NONE) }
          val offsetY = remember { Animatable(0f) }
          val onDragEnd = {
            coroutineScope.launch {
              offsetY.animateTo(0f, spring(0.7f, 500f))
            }
          }
          var currentZone by remember { mutableStateOf(GestureZone.NONE) }
          var currentDirection by remember { mutableStateOf(GestureDirection.NONE) }

          val superFastDpSpec: AnimationSpec<Dp> = tween(durationMillis = 300)
          val fastDpSpec: AnimationSpec<Dp> = tween(durationMillis = 500)
          val slowDpSpec: AnimationSpec<Dp> = tween(durationMillis = 1000)
          val fastFloatSpec: AnimationSpec<Float> = tween(durationMillis = 500)
          val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
          val appListOffset by animateDpAsState(
            targetValue = if (screenState.isApps()) 0.dp else -screenHeight.dp,
            label = "",
            animationSpec = if (screenState.isApps()) superFastDpSpec else slowDpSpec
          )
          val searchBarOffset by animateDpAsState(
            targetValue = if (screenState.isApps()) 0.dp else searchHeight.dp,
            label = "",
            animationSpec = if (screenState.isApps()) fastDpSpec else slowDpSpec
          )
          val appsStateAlpha by animateFloatAsState(
            targetValue = if (screenState.isApps()) 1f else 0f,
            label = "",
            animationSpec = if (screenState.isApps()) fastFloatSpec else slowFloatSpec
          )
          val favoritesAlpha by animateFloatAsState(
            targetValue =
            if (screenState.isApps()) 0f else 1f,
            label = "",
            animationSpec = if (screenState.isApps()) fastFloatSpec else slowFloatSpec
          )

          CompositionLocalProvider(LocalRippleTheme provides ClearRippleTheme) {
            Column(
              modifier = Modifier
                .constrainAs(favList) {
                  top.linkTo(parent.top)
                  bottom.linkTo(parent.bottom)
                  start.linkTo(parent.start)
                  end.linkTo(parent.end)
                }
                .fillMaxSize()
                .graphicsLayer {
                  alpha = favoritesAlpha
                }
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
                      viewModel.onEvent(Event.HandleGesture(gesture))
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
                      viewModel.onEvent(Event.HandleGesture(gesture))
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
              Text(totalUsage.toTimeUsed())
              val data = remember { mutableStateOf(favorites) }
              LaunchedEffect(favorites) {
                if (favorites.size != data.value.size) {
                  data.value = favorites
                }
              }
              val reorderableState = rememberReorderableLazyListState(
                onMove = { from, to ->
                  data.value = data.value.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                  }
                  viewModel.onEvent(Event.UpdateFavoriteOrder(data.value))
                }
              )
              LazyColumn(
                state = reorderableState.listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                userScrollEnabled = false,
                modifier = Modifier
                  .fillMaxWidth()
                  .thenIf(screenState.isModify()) { detectReorder(reorderableState) }
                  .thenIf(screenState.isModify()) { reorderable(reorderableState) }
              ) {
                items(data.value, { it.app.id }) { item ->
                  ReorderableItem(reorderableState, key = item.app.id) { isDragged ->
                    val appUsage by viewModel.getUsageForApp(item.app)
                    AppCard(
                      appTitle = item.app.appTitle,
                      appUsage = appUsage,
                      editState = screenState.isModify(),
                      isDragged = isDragged
                    ) {
                      if (screenState.isFavorites()) {
                        viewModel.onEvent(Event.OpenApplication(item.app))
                      }
                    }
                  }
                }
              }
              val density = LocalDensity.current
              val bottomHeightDp = with(density) { bottomHeight.toDp() }
              Spacer(modifier = Modifier.height(bottomHeightDp))
            }
          }

          LazyColumn(
            state = appListState,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            modifier = Modifier
              .constrainAs(appList) {
                top.linkTo(parent.top)
                bottom.linkTo(searchBar.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
              }
              .graphicsLayer {
                alpha = appsStateAlpha
              }
              .offset(y = appListOffset)
          ) {
            items(items = apps, key = { it.id }) { app ->
              val appTitle = app.appTitle
              val appUsage by viewModel.getUsageForApp(app)
              Box(
                modifier = Modifier.animateItemPlacement(
                  animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
                )
              ) {
                AppCard(appTitle, appUsage) { viewModel.onEvent(Event.OpenApplication(app)) }
              }
            }
          }

          Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
              .constrainAs(searchBar) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
              }
              .graphicsLayer {
                alpha = appsStateAlpha
              }
              .padding(horizontal = 32.dp)
              .padding(bottom = 32.dp)
              .onGloballyPositioned {
                searchHeight = it.size.height.toFloat()
              }
              .offset(y = searchBarOffset)
          ) {
            TextField(
              value = searchText,
              onValueChange = { viewModel.onEvent(Event.UpdateSearch(it)) },
              modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clearFocusOnKeyboardDismiss(),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions = KeyboardActions(onSearch = {
                apps.firstOrNull()?.let {
                  viewModel.onEvent(Event.OpenApplication(it))
                }
              }),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
              ),
              placeholder = {
                Text(
                  text = "search",
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth()
                )
              },
              textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
          }
        }
      }
    }
  }
}
