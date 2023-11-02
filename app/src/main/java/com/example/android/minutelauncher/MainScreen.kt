package com.example.android.minutelauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App
import com.example.android.minutelauncher.reorderableList.ReorderableItem
import com.example.android.minutelauncher.reorderableList.detectReorder
import com.example.android.minutelauncher.reorderableList.rememberReorderableLazyListState
import com.example.android.minutelauncher.reorderableList.reorderable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val appSelectorVisible = remember { mutableStateOf(false) }
  val selectedDirection = remember { mutableStateOf<Gesture?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val screenState by viewModel.screenState.collectAsState()
  val currentAppModal by viewModel.currentModal.collectAsState()
  val dialogSheetScaffoldState =
    rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      confirmValueChange = {
        if (it == ModalBottomSheetValue.Hidden) viewModel.onEvent(Event.ClearModal)
        true
      })
  val backgroundColor by animateColorAsState(
    targetValue = when (screenState) {
      ScreenState.FAVORITES -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      ScreenState.MODIFY -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
      ScreenState.APPS -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    },
    label = ""
  )

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

  LaunchedEffect(key1 = screenState) {
    when (screenState) {
      ScreenState.APPS -> {
        try {
          focusRequester.requestFocus()
        } catch (e: Exception) {
          Timber.d("Request focus failed: not in composition?")
        }
      }

      else -> Unit
    }
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
      ScreenState.FAVORITES -> Unit
      else -> viewModel.onEvent(Event.UpdateSearch(""))
    }
  }


  val searchText by viewModel.searchTerm.collectAsState()
  val apps by viewModel.filteredApps.collectAsState(initial = emptyList())

  val fadeInDuration = 500
  val fadeOutDuration = 200
  val fadeEasing = FastOutSlowInEasing


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
        BackHandler(true) {
          viewModel.onEvent(Event.ClearModal)
        }
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
      Surface(
        color = backgroundColor,
      ) {
        Box(
          modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
        ) {
          AnimatedVisibility(
            visible = screenState == ScreenState.FAVORITES || screenState == ScreenState.MODIFY,
            enter = fadeIn(tween(fadeInDuration, 0, fadeEasing)),
            exit = fadeOut(tween(fadeOutDuration, 0, fadeEasing))
          ) {
            BoxWithConstraints(
              modifier = Modifier.align(Alignment.TopCenter)
            ) {
              val sideWidth by animateDpAsState(
                targetValue =
                when (screenState) {
                  ScreenState.MODIFY -> 60.dp
                  else -> 0.dp
                }, label = "",
                animationSpec = tween(200, 0, EaseInOutQuad)
              )
              val middleWidth = maxWidth - sideWidth.times(2)
              val zoneHeight = maxHeight.div(2)
              val bottomHeight = maxHeight.div(6)

              val totalUsage by viewModel.getTotalUsage()
              val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
              val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
              var gesture by remember { mutableStateOf(Gesture.NONE) }

              val offsetY = remember { Animatable(0f) }
              val onDragEnd = {
                coroutineScope.launch {
                  offsetY.animateTo(
                    0f, spring(0.7f, 500f)
                  )
                }
              }
              var currentZone by remember { mutableStateOf(GestureZone.NONE) }
              var currentDirection by remember { mutableStateOf(GestureDirection.NONE) }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(LocalConfiguration.current.screenHeightDp.dp)
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
                      currentZone = if (change.position.y < zoneHeight.value) {
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
                      val easingFactor = (1 - weight * 0.75f) * 0.5f
                      val easedDragAmount = dragAmount * easingFactor
                      coroutineScope.launch {
                        offsetY.snapTo(originalY + easedDragAmount)
                      }
                      gesture = if (easingFactor < 0.2) {
                        if (offsetY.value > 0) Gesture.DOWN else Gesture.UP
                      } else Gesture.NONE
                    }
                  },
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                GestureColumn(
                  height = zoneHeight,
                  width = sideWidth,
                  side = Alignment.Start,
                  gestureApps = gestureApps,
                  onClick = {
                    selectedDirection.value = it
                    Timber.d("Selected direction: ${selectedDirection.value}")
                    appSelectorVisible.value = true
                  }
                )
                CompositionLocalProvider(LocalRippleTheme provides ClearRippleTheme) {
                  Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                      .fillMaxHeight()
                      .width(middleWidth)
                      .combinedClickable(onLongClick = {
                        Timber.d("Long press on home screen")
                        viewModel.onEvent(Event.ChangeScreenState(screenState.toggleModify()))
                      }) {}
                  ) {
                    AnimatedVisibility(
                      visible = !screenState.isModify(),
                      enter = fadeIn(tween(500, 600)),
                      exit = fadeOut(tween(150))
                    ) {
                      Text(totalUsage.toTimeUsed())
                    }
                    val data = remember { mutableStateOf(favorites) }
                    LaunchedEffect(favorites) {
                      if (favorites.size != data.value.size) {
                        data.value = favorites
                      }
                    }
                    Timber.d("Favorites size: ${favorites.size} Data size: ${data.value.size}")
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
                    Spacer(modifier = Modifier.height(bottomHeight))
                  }
                }
                GestureColumn(
                  height = zoneHeight,
                  width = sideWidth,
                  side = Alignment.End,
                  gestureApps = gestureApps
                ) {
                  selectedDirection.value = it
                  Timber.d("Selected direction: ${selectedDirection.value}")
                  appSelectorVisible.value = true
                }
              }
            }
          }
          AnimatedVisibility(
            visible = screenState == ScreenState.APPS,
            enter = fadeIn(tween(fadeInDuration, 0, fadeEasing)),
            exit = fadeOut(tween(fadeOutDuration, 0, fadeEasing))
          ) {
            LazyColumn(
              verticalArrangement = Arrangement.Bottom,
              reverseLayout = true,
              modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
            ) {
              stickyHeader {
                Surface(
                  shape = MaterialTheme.shapes.large,
                  modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
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
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                  )
                }
              }
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
          }
        }
      }
    }
    AnimatedVisibility(
      visible = appSelectorVisible.value,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface {
        AppList(
          onAppPress = { app ->
            coroutineScope.launch {
              Timber.d("App pressed: ${app.appTitle}")
              selectedDirection.value?.let {
                Timber.d("Selected ${app.appTitle} in direction $it")
                viewModel.onEvent(Event.SetAppGesture(app, it))
              }
              appSelectorVisible.value = false
              delay(500L)
              viewModel.onEvent(Event.UpdateSearch(""))
            }
          }
        ) {
          coroutineScope.launch {
            appSelectorVisible.value = false
            delay(500L)
            viewModel.onEvent(Event.UpdateSearch(""))
          }
        }
      }
    }
  }
}

@Composable
fun GestureColumn(
  height: Dp,
  width: Dp,
  side: Alignment.Horizontal,
  gestureApps: Map<Gesture, App?>,
  onClick: (Gesture) -> Unit,
) {
  var (topStart, topEnd, bottomStart, bottomEnd) = listOf(0.dp, 0.dp, 0.dp, 0.dp)
  val corner = 64.dp
  var apps = mutableMapOf<Gesture, App?>()
  when (side) {
    Alignment.Start -> {
      topEnd = corner
      bottomEnd = corner
      apps = mutableMapOf(
        Gesture.UPPER_RIGHT to gestureApps[Gesture.UPPER_RIGHT],
        Gesture.LOWER_RIGHT to gestureApps[Gesture.LOWER_RIGHT]
      )
    }

    Alignment.End -> {
      bottomStart = corner
      topStart = corner

      apps = mutableMapOf(
        Gesture.UPPER_LEFT to gestureApps[Gesture.UPPER_LEFT],
        Gesture.LOWER_LEFT to gestureApps[Gesture.LOWER_LEFT]
      )
    }
  }

  Column(
    modifier = Modifier
      .width(width)
      .fillMaxHeight()
  ) {
    apps.forEach { entry ->
      val app = entry.value
      val direction = entry.key
      val title = app?.appTitle ?: ""
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(height)
          .padding(vertical = 24.dp),
        shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        onClick = {
          onClick(direction)
        }
      ) {
        Column(
          verticalArrangement = Arrangement.Center,
        ) {
          title.forEach { char ->
            Text(
              text = char.toString(),
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

inline fun Modifier.thenIf(
  condition: Boolean,
  crossinline other: Modifier.() -> Modifier,
) = if (condition) other() else this


@SuppressLint("WrongConstant")
fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
  try {
    val statusBarService = context.getSystemService("statusbar")
    val methodName = if (expand) "expandNotificationsPanel" else "collapsePanels"
    val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
    val method: Method = statusBarManager.getMethod(methodName)
    method.invoke(statusBarService)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

object ClearRippleTheme : RippleTheme {
  @Composable
  override fun defaultColor(): Color = Color.Transparent

  @Composable
  override fun rippleAlpha() = RippleAlpha(
    draggedAlpha = 0f, focusedAlpha = 0f, hoveredAlpha = 0f, pressedAlpha = 0f
  )
}
