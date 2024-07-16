package com.alveteg.simon.minutelauncher.home

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.dashboard.Dashboard
import com.alveteg.simon.minutelauncher.home.modal.AppModalBottomSheet
import timber.log.Timber
import java.lang.reflect.Method
import java.time.LocalDate

@Composable
fun HomeScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: HomeViewModel = hiltViewModel()
) {
  var screenState by rememberSaveable { mutableStateOf(ScreenState.FAVORITES) }
  val searchText by viewModel.searchTerm.collectAsState()
  val apps by viewModel.filteredApps.collectAsState()
  val installedApps by viewModel.installedApps.collectAsState()
  val timerMappings by viewModel.accessTimerMappings.collectAsState()
  val totalUsage by remember {
    derivedStateOf {
      installedApps.sumOf {
        it.usage.firstOrNull { it.usageDate == LocalDate.now() }?.usageDuration ?: 0L
      }
    }
  }
  val favorites by viewModel.favoriteApps.collectAsState()

  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  var currentAppPackage by remember { mutableStateOf<String?>(null) }
  val currentAppModal by remember {
    derivedStateOf { apps.firstOrNull { it.app.packageName == currentAppPackage } }
  }

  val backgroundColor by animateColorAsState(
    targetValue = when (screenState) {
      ScreenState.FAVORITES -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      ScreenState.DASHBOARD -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
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
        is UiEvent.ShowModal -> currentAppPackage = event.appInfo.app.packageName
        is UiEvent.ShowDashboard -> screenState = ScreenState.DASHBOARD
        is UiEvent.Navigate -> onNavigate(event)
      }
    }
  }


  BackHandler(true) {
    screenState = ScreenState.FAVORITES
  }

  AppModalBottomSheet(
    appInfo = currentAppModal,
    timerMappings = timerMappings,
    onDismiss = { currentAppPackage = null },
    onEvent = viewModel::onEvent
  )

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
    var screenHeight by remember { mutableFloatStateOf(0f) }
    Surface(color = backgroundColor,
      modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned {
          screenHeight = it.size.height.toFloat()
        }) {
      Box(
        modifier = Modifier.fillMaxSize()
      ) {
        val offsetY = remember { Animatable(0f) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val appListSelectionAction: (AppInfo) -> Unit = {
          viewModel.onEvent(HomeEvent.OpenApplication(it))
          keyboardController?.hide()
        }

        FavoriteList(
          screenState = screenState,
          favorites = favorites,
          onEvent = viewModel::onEvent,
          screenHeight = screenHeight,
          totalUsage = totalUsage,
          offsetY = offsetY,
          onAppClick = appListSelectionAction
        )

        Dashboard(
          screenState = screenState,
          onEvent = viewModel::onEvent,
          searchText = searchText,
          onAppClick = { appListSelectionAction(it) },
          apps = apps,
          offsetY = offsetY,
          usageStatistics = installedApps.flatMap { it.usage },
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