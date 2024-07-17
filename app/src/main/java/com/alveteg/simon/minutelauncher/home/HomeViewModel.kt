package com.alveteg.simon.minutelauncher.home

import android.content.pm.LauncherApps
import android.os.UserHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.ApplicationRepository
import com.alveteg.simon.minutelauncher.data.FavoriteAppInfo
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.settings.SettingsScreen
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val applicationRepository: ApplicationRepository
) : ViewModel() {

  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow().onEach { Timber.d(it.toString()) }

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  val favoriteApps = combine(
    roomRepository.favoriteApps(), applicationRepository.usageStats
  ) { favorites, usageStats ->
    favorites.map { favorite ->
      val usage = usageStats.filter { favorite.app.packageName == it.packageName }
      FavoriteAppInfo(favorite, usage)
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val installedApps = combine(
    roomRepository.appList(), roomRepository.favoriteApps(), applicationRepository.usageStats
  ) { apps, favorites, usageStats ->
    apps.map { app ->
      val favorite = favorites.map { it.app.packageName }.contains(app.packageName)
      val usage = usageStats.filter { app.packageName == it.packageName }
      AppInfo(app, favorite, usage)
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val accessTimerMappings = roomRepository.getAccessTimerMappings()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  private val packageCallback = object : LauncherApps.Callback() {
    override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
      Timber.d("Package Removed")
      updateDatabase()
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
      Timber.d("Package Added")
      updateDatabase()
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
      Timber.d("Package Changed")
      updateDatabase()
    }

    override fun onPackagesAvailable(
      p1: Array<out String>?, p2: UserHandle?, p3: Boolean
    ) {
    }

    override fun onPackagesUnavailable(
      p1: Array<out String>?, p2: UserHandle?, p3: Boolean
    ) {
    }
  }

  init {
    Timber.d("ViewModel initialized!")
    applicationRepository.registerCallback(packageCallback)
    updateDatabase()
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        applicationRepository.startUsageUpdater()
      }
    }
  }

  private fun updateDatabase() {
    Timber.d("Update Database Called")
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val currentApps = roomRepository.appList().first()
        val installedApps = applicationRepository.getApps()
        val currentAppPackageNames = currentApps.map { it.packageName }.toSet()
        val installedAppPackageNames = installedApps.map { it.packageName }.toSet()
        val newApps = installedApps.filter { !currentAppPackageNames.contains(it.packageName) }
        val removedApps = currentApps.filter { !installedAppPackageNames.contains(it.packageName) }

        Timber.d("${newApps.size} new apps and ${removedApps.size} removed apps found.")

        newApps.forEach { roomRepository.insertApp(it) }
        removedApps.forEach { roomRepository.removeApp(it) }
      }
    }
  }

  fun onEvent(event: Event) {
    Timber.d(event.toString())
    when (event) {
      is HomeEvent.ShowToast -> sendUiEvent(UiEvent.ShowToast(event.text, event.length))
      is HomeEvent.OpenApplication -> {
        sendUiEvent(UiEvent.ShowModal(event.appInfo))
        sendUiEvent(UiEvent.VibrateLongPress)
      }

      is HomeEvent.LaunchActivity -> {
        val appInfo = event.appInfo
        Timber.d("Launch Activity ${appInfo.app.appTitle}")
        applicationRepository.getLaunchIntentForPackage(appInfo.app.packageName)?.let { intent ->
          sendUiEvent(UiEvent.LaunchActivity(intent))
          viewModelScope.launch {
            delay(100)
            _searchTerm.value = ""
          }
        }
      }

      is HomeEvent.UpdateSearch -> {
        Timber.d("Update search with ${event.searchTerm}")
        _searchTerm.value = event.searchTerm
      }

      is HomeEvent.ToggleFavorite -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            Timber.d("Toggle favorite app ${event.app.appTitle}")
            roomRepository.toggleFavorite(event.app)
          }
        }
      }

      is HomeEvent.HandleGesture -> {
        val gesture = event.gesture
        Timber.d("Gesture handled, $gesture")
        when (gesture) {
          Gesture.UP -> {
            sendUiEvent(UiEvent.ShowDashboard)
            sendUiEvent(UiEvent.VibrateLongPress)
          }

          Gesture.DOWN -> {
            sendUiEvent(UiEvent.ExpandNotifications)
            sendUiEvent(UiEvent.VibrateLongPress)
          }

          else -> {
            viewModelScope.launch {
              withContext(Dispatchers.IO) {
                roomRepository.getAppInfoForGesture(gesture)?.let {
                  getAppInfoForApp(it.app)?.let { app ->
                    sendUiEvent(UiEvent.ShowModal(app))
                    sendUiEvent(UiEvent.VibrateLongPress)
                  }
                }
              }
            }
          }
        }
      }


      is HomeEvent.OpenGestureSettings -> sendUiEvent(UiEvent.Navigate(SettingsScreen.GESTURE_SETTINGS))
      is HomeEvent.OpenTimerSettings -> sendUiEvent(UiEvent.Navigate(SettingsScreen.TIMER_SETTINGS))
      is HomeEvent.UpdateFavoriteOrder -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateFavoritesOrder(event.favorites)
          }
        }
      }

      is HomeEvent.UpdateApp -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateApp(event.app)
          }
        }
      }
    }
  }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }

  private suspend fun getAppInfoForApp(app: App): AppInfo? {
    return installedApps.first().find { it.app.packageName == app.packageName }
  }

  override fun onCleared() {
    Timber.d("HomeViewModel Cleared.")
    applicationRepository.unregisterCallback()
    super.onCleared()
  }
}