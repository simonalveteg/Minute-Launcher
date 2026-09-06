package com.alveteg.simon.minutelauncher.home

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.sumOf
import com.alveteg.simon.minutelauncher.home.dashboard.Dashboard
import com.alveteg.simon.minutelauncher.home.modal.AppModalBottomSheet
import com.alveteg.simon.minutelauncher.home.modal.MinuteBottomSheet
import com.alveteg.simon.minutelauncher.home.onboarding.Onboarding
import com.alveteg.simon.minutelauncher.settings.components.GestureInput
import com.alveteg.simon.minutelauncher.utilities.Gesture
import timber.log.Timber
import java.lang.reflect.Method
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: HomeViewModel = hiltViewModel()
) {
  var screenState by rememberSaveable { mutableStateOf(ScreenState.FAVORITES) }
  val searchText by viewModel.searchTerm.collectAsState()
  val apps by viewModel.filteredApps.collectAsState()
  val installedApps by viewModel.installedApps.collectAsState()
  val totalUsage by remember(installedApps) {
    derivedStateOf {
      installedApps.sumOf {
        it.usage.firstOrNull { it.usageDate == LocalDate.now() }?.usageDuration
      }
    }
  }
  val favorites by viewModel.favoriteApps.collectAsState()

  val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
  val showDefaultHomePrompt by viewModel.showDefaultHomePrompt.collectAsStateWithLifecycle()
  val showAdminAccessPrompt by viewModel.showAdminAccessPrompt.collectAsStateWithLifecycle()
  val showUsageAccessPrompt by viewModel.showUsageAccessPrompt.collectAsStateWithLifecycle()
  val skipAppModal by viewModel.skipAppModal.collectAsStateWithLifecycle()
  val backgroundTransparency by viewModel.transparencyAmount.collectAsStateWithLifecycle()
  val defaultMindfulDelayLength by viewModel.mindfulDelayLength.collectAsStateWithLifecycle()
  val backgroundAlpha by remember(backgroundTransparency) { derivedStateOf { (1f - backgroundTransparency) } }
  val altBackgroundAlpha by remember(backgroundAlpha) { derivedStateOf { backgroundAlpha + (1f - backgroundAlpha) * 0.66f } }

  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  var currentAppPackage by remember { mutableStateOf<String?>(null) }
  val currentAppModal by remember(currentAppPackage) {
    derivedStateOf { apps.firstOrNull { it.app.packageName == currentAppPackage } }
  }
  var showGestureModal by remember { mutableStateOf(Gesture.NONE) }


  val backgroundColor by animateColorAsState(
    targetValue = when (screenState) {
      ScreenState.FAVORITES -> MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha)
      ScreenState.DASHBOARD -> MaterialTheme.colorScheme.surface.copy(alpha = altBackgroundAlpha)
      ScreenState.APPS -> MaterialTheme.colorScheme.surface.copy(alpha = altBackgroundAlpha)
    },
    label = ""
  )

  LaunchedEffect(key1 = true) {
    Timber.d("launched effect")
    viewModel.uiEvent.collect { event ->
      Timber.d("event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.VibrateLongPress -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.ExpandNotifications -> setExpandNotificationDrawer(mContext, true)
        is UiEvent.ShowModal -> {
          if (skipAppModal && event.appInfo.mindfulDelay == 0) {
            viewModel.onEvent(HomeEvent.LaunchActivity(event.appInfo))
          } else {
            currentAppPackage = event.appInfo.app.packageName
          }
        }

        is UiEvent.TriggerGesture -> {
          if (event.appInfo != null) {
            viewModel.onEvent(HomeEvent.ShowModal(event.appInfo))
          } else {
            showGestureModal = event.gesture
            Timber.d("Gesture triggered: $showGestureModal")
          }
        }

        is UiEvent.ShowDashboard -> screenState = ScreenState.DASHBOARD
        is UiEvent.ShowFavorites -> screenState = ScreenState.FAVORITES
        is UiEvent.Navigate -> onNavigate(event)
      }
    }
  }

  BackHandler(true) {
    screenState = ScreenState.FAVORITES
  }

  AppModalBottomSheet(
    appInfo = currentAppModal,
    defaultMindfulDelayLength = defaultMindfulDelayLength,
    onDismiss = { currentAppPackage = null },
    onEvent = viewModel::onEvent
  )

  if (showGestureModal != Gesture.NONE) {
    MinuteBottomSheet(
      onDismissRequest = { showGestureModal = Gesture.NONE },
      title = stringResource(R.string.title_unset_gesture),
      description = stringResource(R.string.description_unset_gesture)
    ) {
      GestureInput(
        gesture = showGestureModal,
        iconResource = showGestureModal.getIcon(),
        onEvent = viewModel::onEvent,
        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 46.dp)
      ) {
        showGestureModal = Gesture.NONE
      }
    }
  }

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
    AnimatedContent(
      targetState = showOnboarding,
      transitionSpec = {
        fadeIn(animationSpec = tween(500))
          .togetherWith(fadeOut(animationSpec = tween(1000)))
      },
      label = "onboarding_transition"
    ) { targetShowOnboarding ->
      if (targetShowOnboarding) {
        Onboarding(
          apps = apps,
          onEvent = viewModel::onEvent
        )
      } else {
        Surface(
          color = backgroundColor,
          modifier = Modifier
            .fillMaxSize()
        ) {
          val offsetY = remember { Animatable(0f) }
          val keyboardController = LocalSoftwareKeyboardController.current
          val appListSelectionAction: (AppInfo) -> Unit = {
            Timber.d("App selected: $it")
            viewModel.onEvent(HomeEvent.OpenApplication(it))
            keyboardController?.hide()
          }

          FavoriteList(
            screenState = screenState,
            favorites = favorites,
            onEvent = viewModel::onEvent,
            totalUsage = totalUsage,
            offsetY = offsetY,
            showDefaultHomePrompt = showDefaultHomePrompt,
            showAdminAccessPrompt = showAdminAccessPrompt,
            showUsageAccessPrompt = showUsageAccessPrompt,
            onAppClick = appListSelectionAction
          )

          Dashboard(
            screenState = screenState,
            onEvent = viewModel::onEvent,
            searchText = searchText,
            onAppClick = appListSelectionAction,
            apps = apps,
            offsetY = offsetY,
            onSearch = {
              apps.firstOrNull()?.let {
                appListSelectionAction(it)
              }
              this.defaultKeyboardAction(ImeAction.Done)
            }
          )
        }
      }
    }
  }
}

fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
  try {
    val statusBarService = context.getSystemService(Context.STATUS_BAR_SERVICE)
    val methodName = if (expand) "expandNotificationsPanel" else "collapsePanels"
    val statusBarManager = Class.forName("android.app.StatusBarManager")
    val method: Method = statusBarManager.getMethod(methodName)
    method.isAccessible = true
    method.invoke(statusBarService)
  } catch (e: Exception) {
    Timber.e(e, "Failed to toggle notification drawer")
  }
}