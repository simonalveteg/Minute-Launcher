package com.alveteg.simon.minutelauncher.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.ApplicationRepository
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.data.SwipeApp
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val applicationRepository: ApplicationRepository
) : ViewModel() {

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  val gestureApps = roomRepository.gestureApps()

  val accessTimerMappings = roomRepository.getAccessTimerMappings()

  val installedApps = combine(
    roomRepository.appList(), roomRepository.favoriteApps(), applicationRepository.usageStats
  ) { apps, favorites, usageStats ->
    apps.map { app ->
      val favorite = favorites.map { it.app.packageName }.contains(app.packageName)
      val usage = usageStats.filter { app.packageName == it.packageName }
      AppInfo(app, favorite, usage)
    }
  }
  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }
  val defaultTimerApps = installedApps.transform { appList ->
    emit(appList.filter { it.app.timer == AccessTimer.DEFAULT }
      .sortedBy { it.app.appTitle.lowercase() })
  }
  val nonDefaultTimerApps = installedApps.transform { appList ->
    emit(appList.filter { it.app.timer != AccessTimer.DEFAULT }
      .sortedBy { it.app.appTitle.lowercase() })
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
      is HomeEvent.UpdateApp -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateApp(event.app)
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

      is SettingsEvent.SetDefaultTimer -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.setAccessTimerMapping(event.accessTimerMapping)
          }
        }
      }

      is SettingsEvent.SetAppGesture -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.insertGestureApp(SwipeApp(event.gesture, event.app))
          }
        }
        sendUiEvent(UiEvent.Navigate(route = SettingsScreen.GESTURE_SETTINGS, popBackStack = true))
      }

      is SettingsEvent.OpenGestureList -> sendUiEvent(UiEvent.Navigate(SettingsScreen.GESTURE_SETTINGS_LIST + "/${event.gesture}"))

    }
  }
}