package com.example.android.minutelauncher.home

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.Event
import com.example.android.minutelauncher.utilities.Gesture
import com.example.android.minutelauncher.data.LauncherViewModel
import com.example.android.minutelauncher.UiEvent
import com.example.android.minutelauncher.data.App
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.reflect.Method

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val screenState by viewModel.screenState.collectAsState()
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val selectorListState = rememberLazyListState()
  val selectedGesture = remember { mutableStateOf<Gesture?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val currentAppModal by viewModel.currentModal.collectAsState()
  val searchText by viewModel.searchTerm.collectAsState()
  val apps by viewModel.filteredApps.collectAsState(initial = emptyList())
  var screenHeight by remember { mutableFloatStateOf(0f) }
  val shortcutSelectionAction: (App) -> Unit = { app ->
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
    keyboardController?.hide()
  }

  val backgroundColor by animateColorAsState(
    targetValue = when (screenState) {
      ScreenState.FAVORITES -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      ScreenState.MODIFY -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
      ScreenState.APPS -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
      ScreenState.SELECTOR -> MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    },
    label = ""
  )
  val sheetState = rememberModalBottomSheetState()

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

  LaunchedEffect(key1 = screenState) {
    Timber.d("ScreenState is now $screenState")
    when (screenState) {
      ScreenState.FAVORITES -> {
        delay(500)
        selectorListState.scrollToItem(0)
      }

      ScreenState.SELECTOR -> {
        delay(500)
        selectorListState.scrollToItem(0)
      }

      else -> viewModel.onEvent(Event.UpdateSearch(""))
    }
  }

  MinuteBottomSheet(
    app = currentAppModal,
    sheetState = sheetState,
    onDismiss = { viewModel.onEvent(Event.ClearModal) },
    onEvent = viewModel::onEvent
  )

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

        val totalUsage by viewModel.getTotalUsage()
        val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
        val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())

        val superFastDpSpec: AnimationSpec<Dp> = tween(durationMillis = 200)
        val fastSpringDpSpec: AnimationSpec<Dp> = tween(
          durationMillis = 300,
          easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
        )
        val slowDpSpec: AnimationSpec<Dp> = tween(durationMillis = 1000)
        val fastFloatSpec: AnimationSpec<Float> = tween(durationMillis = 500)
        val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
        val appListOffset by animateDpAsState(
          targetValue = if (screenState.isApps()) 0.dp else -screenHeight.dp,
          label = "",
          animationSpec = if (screenState.isApps()) fastSpringDpSpec else slowDpSpec
        )
        val appSelectorOffset by animateDpAsState(
          targetValue = if (screenState.isSelector()) 0.dp else -screenHeight.dp,
          label = "",
          animationSpec = if (screenState.isSelector()) superFastDpSpec else slowDpSpec
        )
        val appsStateAlpha by animateFloatAsState(
          targetValue = if (screenState.isApps()) 1f else 0f,
          label = "",
          animationSpec = if (screenState.isApps()) fastFloatSpec else slowFloatSpec
        )
        val appSelectorAlpha by animateFloatAsState(
          targetValue = if (screenState.isSelector()) 1f else 0f,
          label = "",
          animationSpec = if (screenState.isSelector()) fastFloatSpec else slowFloatSpec
        )

        val appListSelectionAction: (App) -> Unit = {
          viewModel.onEvent(Event.OpenApplication(it))
          keyboardController?.hide()
        }

        FavoriteList(
          screenState = screenState,
          favorites = favorites,
          constraintReference = favList,
          constraints = {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(topLeft.end)
            end.linkTo(topRight.start)
            width = Dimension.fillToConstraints
          },
          onEvent = viewModel::onEvent,
          screenHeight = screenHeight,
          totalUsage = totalUsage,
          onAppClick = appListSelectionAction
        )

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
          onAppClick = { shortcutSelectionAction(it) },
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
          onAppClick = { appListSelectionAction(it) }
        )

        SearchBar(
          screenState = screenState,
          constraintReference = searchBar,
          constraints = {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
          },
          onEvent = viewModel::onEvent,
          text = searchText,
          onSearch = {
            apps.firstOrNull()?.let {
              if (screenState.isSelector()) shortcutSelectionAction(it) else appListSelectionAction(
                it
              )
            }
            this.defaultKeyboardAction(ImeAction.Done)
          }
        )

        GestureApps(
          screenState = screenState,
          screenHeight = screenHeight,
          apps = gestureApps,
          onClick = {
            selectedGesture.value = it
            Timber.d("Selected direction: ${selectedGesture.value}")
          },
          crTopLeft = topLeft,
          crTopRight = topRight,
          crBottomLeft = bottomLeft,
          crBottomRight = bottomRight
        )

        createVerticalChain(topLeft, bottomLeft, chainStyle = ChainStyle.Spread)
        createVerticalChain(topRight, bottomRight, chainStyle = ChainStyle.Spread)
      }
    }
  }
}

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