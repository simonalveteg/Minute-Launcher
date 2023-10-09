package com.example.android.minutelauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App
import com.example.android.minutelauncher.reorderableList.ReorderableItem
import com.example.android.minutelauncher.reorderableList.detectReorder
import com.example.android.minutelauncher.reorderableList.rememberReorderableLazyListState
import com.example.android.minutelauncher.reorderableList.reorderable
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.reflect.Method


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
  onNavigate: (String) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val coroutineScope = rememberCoroutineScope()
  val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed) {
      Timber.d(it.name)
      // When sheet is dragged into a collapsed state the keyboard should be hidden
      if (it.name != BottomSheetValue.Expanded.name) {
        focusRequester.freeFocus()
        keyboardController?.hide()
        viewModel.onEvent(Event.UpdateSearch(""))
      }
      true
    }
  )
  val appSelectorVisible = remember { mutableStateOf(false) }
  val selectedDirection = remember { mutableStateOf<GestureDirection?>(null) }

  var currentAppModal by remember { mutableStateOf<App?>(null) }
  val dialogSheetScaffoldState =
    rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      confirmValueChange = {
        if (it == ModalBottomSheetValue.Hidden) currentAppModal = null
        true
      })
  LaunchedEffect(key1 = true) {
    Timber.d("launched effect")
    viewModel.uiEvent.collect { event ->
      Timber.d("event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.OpenApplication -> {
          currentAppModal = event.app
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.OpenAppDrawer -> {
          launch { bottomSheetScaffoldState.bottomSheetState.expand() }
          focusRequester.requestFocus()
        }

        is UiEvent.ExpandNotifications -> {
          setExpandNotificationDrawer(mContext, true)
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
      }
    }
  }

  LaunchedEffect(key1 = currentAppModal) {
    if (currentAppModal != null) {
      launch { dialogSheetScaffoldState.show() }
      keyboardController?.hide()
      focusRequester.freeFocus()
    } else {
      launch { dialogSheetScaffoldState.hide() }
    }
  }

  ModalBottomSheetLayout(
    sheetState = dialogSheetScaffoldState,
    sheetBackgroundColor = MaterialTheme.colorScheme.background,
    sheetContent = {
      Spacer(modifier = Modifier.height(4.dp))
      if (currentAppModal != null) {
        val app = currentAppModal!!
        AppModal(
          app = app,
          onEvent = viewModel::onEvent,
          onConfirmation = {
            viewModel.onEvent(Event.LaunchActivity(app))
            currentAppModal = null
          },
          onDismiss = {
            val isAccessibilityServiceEnabled =
              isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)
            if (isAccessibilityServiceEnabled) {
              MinuteAccessibilityService.turnScreenOff()
              currentAppModal = null
            } else {
              val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
              ContextCompat.startActivity(mContext, intent, null)
            }
          }
        )
      }
    }
  ) {
    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      sheetPeekHeight = 0.dp,
      backgroundColor = Color.Transparent,
      sheetContent = {
        AppList(
          focusRequester = focusRequester,
          onAppPress = { viewModel.onEvent(Event.OpenApplication(it)) },
          onBackPressed = {
            coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
            viewModel.onEvent(Event.UpdateSearch(""))
          }
        )
      },
    ) {
      Surface {
        BoxWithConstraints(
          modifier = Modifier.statusBarsPadding()
        ) {
          var state by remember { mutableStateOf(MainScreenState.NORMAL) }
          val sideWidth by animateDpAsState(
            targetValue =
            when (state) {
              MainScreenState.NORMAL -> 0.dp
              MainScreenState.EDIT -> 60.dp
            }, label = "",
            animationSpec = spring(
              Spring.DampingRatioLowBouncy
            )
          )
          val middleWidth = maxWidth - sideWidth.times(2)
          val gestureHeight = maxHeight.div(2)
          val bottomHeight = maxHeight.div(6)

          val totalUsage by viewModel.getTotalUsage()
          val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
          val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
          var gestureDirection: GestureDirection? = null
          val gestureThreshold = 10f

          Row(
            modifier = Modifier
              .fillMaxSize()
              .thenIf(state.isNormal()) {
                pointerInput(Unit) {
                  detectDragGestures(
                    onDragEnd = {
                      gestureDirection?.let {
                        viewModel.onEvent(Event.HandleGesture(it))
                      }
                    }
                  ) { change, dragAmount ->
                    change.consume()
                    Timber.d("position: ${change.position}") // height: 0-2399f
                    val gestureZone =
                      if (change.position.y < 2399 / 2) GestureZone.UPPER else GestureZone.LOWER
                    gestureHandler(dragAmount, gestureThreshold, gestureZone)?.let {
                      gestureDirection = it
                    }
                  }
                }
              },
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            GestureColumn(
              height = gestureHeight,
              width = sideWidth,
              side = Alignment.Start,
              gestureApps = gestureApps,
              onClick =  {
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
                    state = state.next()
                  }) {}
              ) {
                AnimatedVisibility(
                  visible = !state.isEdit(),
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
                val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
                  data.value = data.value.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                  }
                  viewModel.onEvent(Event.UpdateFavoriteOrder(data.value))
                })
                LazyColumn(
                  state = reorderableState.listState,
                  horizontalAlignment = Alignment.CenterHorizontally,
                  userScrollEnabled = false,
                  modifier = Modifier
                    .fillMaxWidth()
                    .thenIf(state.isEdit()) { detectReorder(reorderableState) }
                    .thenIf(state.isEdit()) { reorderable(reorderableState) }
                ) {
                  items(data.value, { it.app.id }) { item ->
                    ReorderableItem(reorderableState, key = item.app.id) { isDragged ->
                      val appUsage by viewModel.getUsageForApp(item.app)
                      AppCard(
                        appTitle = item.app.appTitle,
                        appUsage = appUsage,
                        state.isEdit(),
                        isDragged
                      ) {
                        if (state == MainScreenState.NORMAL) {
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
              height = gestureHeight,
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
          Timber.d("App pressed: ${app.appTitle}")
          selectedDirection.value?.let {
            Timber.d("Selected ${app.appTitle} in direction $it")
            viewModel.onEvent(Event.SetAppGesture(app, it))
          }
          appSelectorVisible.value = false
          viewModel.onEvent(Event.UpdateSearch(""))
        }
      ) {
        appSelectorVisible.value = false
        viewModel.onEvent(Event.UpdateSearch(""))
      }
    }
  }
}

@Composable
fun GestureColumn(
  height: Dp,
  width: Dp,
  side: Alignment.Horizontal,
  gestureApps: Map<GestureDirection, App?>,
  onClick: (GestureDirection) -> Unit,
) {
  var (topStart, topEnd, bottomStart, bottomEnd) = listOf(0.dp, 0.dp, 0.dp, 0.dp)
  val corner = 64.dp
  var apps = mutableMapOf<GestureDirection, App?>()
  when (side) {
    Alignment.Start -> {
      topEnd = corner
      bottomEnd = corner
      apps = mutableMapOf(
        GestureDirection.UPPER_RIGHT to gestureApps[GestureDirection.UPPER_RIGHT],
        GestureDirection.LOWER_RIGHT to gestureApps[GestureDirection.LOWER_RIGHT]
      )
    }
    Alignment.End -> {
      bottomStart = corner
      topStart = corner

      apps = mutableMapOf(
        GestureDirection.UPPER_LEFT to gestureApps[GestureDirection.UPPER_LEFT],
        GestureDirection.LOWER_LEFT to gestureApps[GestureDirection.LOWER_LEFT]
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
        tonalElevation = 4.dp,
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

enum class MainScreenState {
  NORMAL, EDIT;

  fun next(): MainScreenState {
    return when (this) {
      NORMAL -> EDIT
      EDIT -> NORMAL
    }
  }

  fun isEdit(): Boolean = this == EDIT
  fun isNormal(): Boolean = this == NORMAL
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
