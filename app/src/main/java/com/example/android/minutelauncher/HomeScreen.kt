package com.example.android.minutelauncher

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Shape
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
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
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
fun HomeScreen(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val screenState by viewModel.screenState.collectAsState()
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val density = LocalDensity.current
  val focusRequester = remember { FocusRequester() }
  val appListState = rememberLazyListState()
  val selectorListState = rememberLazyListState()
  val selectedGesture = remember { mutableStateOf<Gesture?>(null) }
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
      ScreenState.SELECTOR -> MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
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
        is UiEvent.VibrateLongPress -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.ExpandNotifications -> setExpandNotificationDrawer(mContext, true)
      }
    }
  }

  BackHandler(true) {
    var nextState = ScreenState.FAVORITES
    when (screenState) {
      ScreenState.SELECTOR -> {
        nextState = ScreenState.MODIFY
        selectedGesture.value = null
      }

      else -> Unit
    }
    viewModel.onEvent(Event.ChangeScreenState(nextState))
  }

  LaunchedEffect(key1 = selectedGesture.value) {
    if (selectedGesture.value == null) return@LaunchedEffect
    when (screenState) {
      ScreenState.MODIFY -> viewModel.onEvent(Event.ChangeScreenState(ScreenState.SELECTOR))
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
    Timber.d("ScreenState is now $screenState")
    when (screenState) {
      ScreenState.FAVORITES -> {
        delay(500)
        appListState.scrollToItem(0)
        selectorListState.scrollToItem(0)
      }

      ScreenState.SELECTOR -> {
        delay(500)
        selectorListState.scrollToItem(0)
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
          val (favList, appList, appSelector, searchBar, topLeft, topRight, bottomLeft, bottomRight) = createRefs()
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

          val superFastDpSpec: AnimationSpec<Dp> = tween(durationMillis = 200)
          val slowDpSpec: AnimationSpec<Dp> = tween(durationMillis = 1000)
          val fastFloatSpec: AnimationSpec<Float> = tween(durationMillis = 500)
          val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
          val appListOffset by animateDpAsState(
            targetValue = if (screenState.isApps()) 0.dp else -screenHeight.dp,
            label = "",
            animationSpec = if (screenState.isApps()) superFastDpSpec else slowDpSpec
          )
          val appSelectorOffset by animateDpAsState(
            targetValue = if (screenState.isSelector()) 0.dp else -screenHeight.dp,
            label = "",
            animationSpec = if (screenState.isSelector()) superFastDpSpec else slowDpSpec
          )
          val searchBarOffset by animateDpAsState(
            targetValue = if (screenState.hasSearch()) 0.dp else searchHeight.dp,
            label = "",
            animationSpec = if (screenState.hasSearch()) superFastDpSpec else slowDpSpec
          )
          val appsStateAlpha by animateFloatAsState(
            targetValue = if (screenState.isApps()) 1f else 0f,
            label = "",
            animationSpec = if (screenState.isApps()) fastFloatSpec else slowFloatSpec
          )
          val searchBarAlpha by animateFloatAsState(
            targetValue = if (screenState.hasSearch()) 1f else 0f,
            label = "",
            animationSpec = if (screenState.hasSearch()) fastFloatSpec else slowFloatSpec
          )
          val appSelectorAlpha by animateFloatAsState(
            targetValue = if (screenState.isSelector()) 1f else 0f,
            label = "",
            animationSpec = if (screenState.isSelector()) fastFloatSpec else slowFloatSpec
          )
          val favoritesAlpha by animateFloatAsState(
            targetValue =
            if (screenState.hasSearch()) 0f else 1f,
            label = "",
            animationSpec = if (screenState.hasSearch()) fastFloatSpec else slowFloatSpec
          )

          CompositionLocalProvider(LocalRippleTheme provides ClearRippleTheme) {
            Column(
              modifier = Modifier
                .constrainAs(favList) {
                  top.linkTo(parent.top)
                  bottom.linkTo(parent.bottom)
                  start.linkTo(topLeft.end)
                  end.linkTo(topRight.start)
                  width = Dimension.fillToConstraints
                }
                .fillMaxSize()
                .graphicsLayer {
                  alpha = favoritesAlpha
                }
                .combinedClickable(onLongClick = {
                  viewModel.onEvent(Event.ChangeScreenState(screenState.toggleModify()))
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
              val bottomHeightDp = with(density) { bottomHeight.toDp() }
              Spacer(modifier = Modifier.height(bottomHeightDp))
            }
          }

          AppList(
            state = selectorListState,
            apps = apps,
            offset = appSelectorOffset,
            alpha = appSelectorAlpha,
            constraintReference = appSelector,
            constraints = {
              top.linkTo(parent.top)
              bottom.linkTo(searchBar.top)
              start.linkTo(topLeft.end)
              end.linkTo(topRight.start)
              height = Dimension.fillToConstraints
            },
            onAppClick = { app ->
              coroutineScope.launch {
                Timber.d("App pressed: ${app.appTitle}")
                selectedGesture.value?.let {
                  Timber.d("Selected ${app.appTitle} in direction $it")
                  viewModel.onEvent(Event.SetAppGesture(app, it))
                }
                viewModel.onEvent(Event.ChangeScreenState(ScreenState.MODIFY))
                delay(500L)
                selectedGesture.value = null
                viewModel.onEvent(Event.UpdateSearch(""))
              }
            },
            header = {
              Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
              ) {
                Text(
                  text = "SELECT SHORTCUT",
                  textAlign = TextAlign.Center,
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                )
              }
            }
          )

          AppList(
            state = appListState,
            apps = apps,
            offset = appListOffset,
            alpha = appsStateAlpha,
            constraintReference = appList,
            constraints = {
              top.linkTo(parent.top)
              bottom.linkTo(searchBar.top)
              start.linkTo(topLeft.end)
              end.linkTo(topRight.start)
              height = Dimension.fillToConstraints
            },
            onAppClick = {
              viewModel.onEvent(Event.OpenApplication(it))
            }
          )

          Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            modifier = Modifier
              .constrainAs(searchBar) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
              }
              .graphicsLayer {
                alpha = searchBarAlpha
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

          gestureApps.forEach { (gesture, app) ->
            val onClick: (Gesture) -> Unit = {
              selectedGesture.value = it
              Timber.d("Selected direction: ${selectedGesture.value}")
            }
            val width = sideWidth.dp
            val height = with(density) { screenHeight.div(2).toDp() - 80.dp }
            val corner = 64.dp
            val leftShape = RoundedCornerShape(0.dp, corner, corner, 0.dp)
            val rightShape = RoundedCornerShape(corner, 0.dp, 0.dp, corner)
            when (gesture) {
              Gesture.UPPER_RIGHT -> {
                GestureCard(
                  app = app,
                  direction = gesture,
                  height = height,
                  width = width,
                  shape = leftShape,
                  constraintReference = topLeft,
                  constraints = {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                  },
                  onClick = onClick
                )
              }

              Gesture.UPPER_LEFT -> {
                GestureCard(
                  app = app,
                  direction = gesture,
                  height = height,
                  width = width,
                  shape = rightShape,
                  constraintReference = topRight,
                  constraints = {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                  },
                  onClick = onClick
                )
              }

              Gesture.LOWER_RIGHT -> {
                GestureCard(
                  app = app,
                  direction = gesture,
                  height = height,
                  width = width,
                  shape = leftShape,
                  constraintReference = bottomLeft,
                  constraints = {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                  },
                  onClick = onClick
                )
              }

              Gesture.LOWER_LEFT -> {
                GestureCard(
                  app = app,
                  direction = gesture,
                  height = height,
                  width = width,
                  shape = rightShape,
                  constraintReference = bottomRight,
                  constraints = {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                  },
                  onClick = onClick
                )
              }

              else -> Unit
            }
          }
          createVerticalChain(topLeft, bottomLeft, chainStyle = ChainStyle.Spread)
          createVerticalChain(topRight, bottomRight, chainStyle = ChainStyle.Spread)
        }
      }
    }
  }
}

@Composable
fun ConstraintLayoutScope.GestureCard(
  app: App,
  direction: Gesture,
  height: Dp,
  width: Dp,
  shape: Shape,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onClick: (Gesture) -> Unit
) {
  val title = app.appTitle
  Surface(
    modifier = Modifier
      .width(width)
      .height(height)
      .padding(vertical = 24.dp)
      .constrainAs(constraintReference) { constraints() },
    shape = shape,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConstraintLayoutScope.AppList(
  state: LazyListState,
  apps: List<App>,
  offset: Dp,
  alpha: Float,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onAppClick: (App) -> Unit,
  header: @Composable () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .constrainAs(constraintReference) { constraints() }
      .graphicsLayer {
        this.alpha = alpha
      }
      .offset(y = offset)
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 12.dp)
    ) {
      header()
    }
    LazyColumn(
      state = state,
      verticalArrangement = Arrangement.Bottom,
      reverseLayout = true,
      modifier = Modifier.fillMaxSize()
    ) {
      items(items = apps, key = { it.id }) { app ->
        val appTitle = app.appTitle
        val appUsage = 0L // todo move into app.usage in viewModel?
        Box(
          modifier = Modifier.animateItemPlacement(
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
          )
        ) {
          AppCard(appTitle, appUsage) { onAppClick(app) }
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