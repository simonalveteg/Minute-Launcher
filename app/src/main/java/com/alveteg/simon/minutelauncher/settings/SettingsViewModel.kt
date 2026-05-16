package com.alveteg.simon.minutelauncher.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.ApplicationRepository
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.data.SwipeApp
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.theme.AppTheme
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val preferenceRepository: PreferenceRepository,
  private val applicationRepository: ApplicationRepository
) : ViewModel() {

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  val gestureApps = roomRepository.gestureApps()
  val appsWithTimers = roomRepository.timerApps()

  val appTheme = preferenceRepository.appTheme
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)
  val useDynamicColor = preferenceRepository.useDynamicColor
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
  val transparencyAmount = preferenceRepository.transparencyAmount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)
  val timerLength = preferenceRepository.timerLength
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

  val installedApps = combine(
    roomRepository.appList(),
    roomRepository.timerApps(),
    roomRepository.favoriteApps(),
    applicationRepository.usageStats,
    timerLength
  ) { apps, timerApps, favorites, usageStats, defaultTimerLength ->
    apps.map { app ->
      val favorite = favorites.map { it.app.packageName }.contains(app.packageName)
      val usage = usageStats.filter { app.packageName == it.packageName }
      val timer = timerApps.find { it.app.packageName == app.packageName }?.appTimer?.timer
        ?: defaultTimerLength
      AppInfo(app, favorite, timer, usage)
    }
  }
  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }

  fun onTimerLengthChange(value: Int) {
    viewModelScope.launch { preferenceRepository.updateTimerLength(value) }
  }

  fun onTransparencyAmountChange(value: Float) {
    viewModelScope.launch { preferenceRepository.updateTransparencyAmount(value) }
  }

  fun onThemeChange(theme: AppTheme) {
    viewModelScope.launch {
      preferenceRepository.updateAppTheme(theme)
    }
  }

  fun onDynamicColorChange(enabled: Boolean) {
    viewModelScope.launch {
      preferenceRepository.updateUseDynamicColor(enabled)
    }
  }

  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow().onEach { Timber.d(it.toString()) }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }

  fun onEvent(event: Event) {
    Timber.d(event.toString())
    when (event) {
      is HomeEvent.UpdateAppTimer -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateAppTimer(event.app, event.timerValue)
          }
        }
      }

      is HomeEvent.ResetAppTimerToDefault -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.removeAppTimer(event.app)
          }
        }
      }

      is HomeEvent.UpdateSearch -> {
        Timber.d("Update search with ${event.searchTerm}")
        _searchTerm.value = event.searchTerm
      }

      is SettingsEvent.ClearAppGesture -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.removeAppForGesture(event.gesture)
          }
        }
      }

      is SettingsEvent.SetAppGesture -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.insertGestureApp(SwipeApp(event.gesture, event.app))
          }
        }
        sendUiEvent(UiEvent.Navigate(route = SettingsScreen.HOME, popBackStack = true))
      }

      is SettingsEvent.OpenGestureList -> sendUiEvent(UiEvent.Navigate(SettingsScreen.GESTURE_SETTINGS_LIST + "/${event.gesture}"))

    }
  }
}