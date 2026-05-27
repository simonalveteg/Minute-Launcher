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
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.settings.SettingsEvent
import com.alveteg.simon.minutelauncher.settings.SettingsScreen
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
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
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val applicationRepository: ApplicationRepository,
  private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow().onEach { Timber.d(it.toString()) }

  val transparencyAmount = preferenceRepository.transparencyAmount
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.TRANSPARENCY_AMOUNT
    )

  val timerLength = preferenceRepository.timerLength
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.TIMER_LENGTH
    )

  val showDefaultHomePrompt = preferenceRepository.showDefaultHomePrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_DEFAULT_HOME_PROMPT
    )

  val showAdminAccessPrompt = preferenceRepository.showAdminAccessPrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_ADMIN_ACCESS_PROMPT
    )

  val showUsageAccessPrompt = preferenceRepository.showUsageAccessPrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_USAGE_ACCESS_PROMPT
    )

  val skipAppModal = preferenceRepository.skipAppModal
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SKIP_APP_MODAL
    )

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  private val _favoriteApps = MutableStateFlow<List<AppInfo>>(emptyList())
  val favoriteApps = _favoriteApps.asStateFlow()


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
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


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
    monitorUsage()

    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        combine(
          roomRepository.favoriteApps(), installedApps
        ) { favorites, apps ->
          favorites.sortedBy { it.favoriteApp.order }.mapNotNull { favorite ->
            apps.find { it.app.packageName == favorite.app.packageName }?.let { app ->
              AppInfo(favorite.app, true, app.timer, app.usage)
            }
          }
        }.collect { favorites ->
          Timber.d("Favorite apps updated: ${favorites.size}")
          _favoriteApps.value = favorites
        }

      }
    }
  }

  private fun monitorUsage() {
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
      is HomeEvent.OpenApplication -> {
        sendUiEvent(UiEvent.ShowModal(event.appInfo))
        sendUiEvent(UiEvent.VibrateLongPress)
      }

      is HomeEvent.LaunchActivity -> {
        val appInfo = event.appInfo
        Timber.d("Launch Activity ${appInfo.app.appTitle}")
        applicationRepository.getLaunchIntentForPackage(appInfo.app.packageName)?.let { intent ->
          sendUiEvent(UiEvent.LaunchActivity(intent))
          sendUiEvent(
            UiEvent.ShowToast(
              "${appInfo.app.appTitle} used for ${
                appInfo.usage.firstOrNull()?.usageDuration.toTimeUsed(false)
              }"
            )
          )
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
                val appInfo = roomRepository.getAppInfoForGesture(gesture)?.let {
                  getAppInfoForApp(it.app)
                }
                sendUiEvent(UiEvent.TriggerGesture(gesture, appInfo))
                sendUiEvent(UiEvent.VibrateLongPress)
              }
            }
          }
        }
      }


      is HomeEvent.OpenTimerSettings -> sendUiEvent(UiEvent.Navigate(SettingsScreen.TIMER_SETTINGS))
      is HomeEvent.OpenSettings -> sendUiEvent(UiEvent.Navigate(SettingsScreen.HOME))
      is HomeEvent.UpdateFavoriteOrder -> {
        val favorites = _favoriteApps.value.toMutableList()
        val reorderedItem = favorites.removeAt(event.from)
        favorites.add(event.to, reorderedItem)

        _favoriteApps.value = favorites.toList()

        Timber.d("Favorite order updated: $favorites")
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateFavoritesOrder(favorites.map { it.app })
          }
        }
      }

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

      is HomeEvent.SetDisplayName -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateApp(event.app.copy(displayTitle = event.displayName))
          }
        }
      }

      is HomeEvent.ResetDisplayName -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateApp(event.app.copy(displayTitle = null))
          }
        }
      }

      is HomeEvent.HideDefaultAppPrompt -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            preferenceRepository.setShowDefaultHomePrompt(false)
          }
        }
      }

      is HomeEvent.HideUsageAccessPrompt -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            preferenceRepository.setShowUsageAccessPrompt(false)
          }
        }
      }

      is HomeEvent.HideAdminAccessPrompt -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            preferenceRepository.setShowAdminAccessPrompt(false)
          }
        }
      }

      is HomeEvent.ShowToast -> sendUiEvent(UiEvent.ShowToast(event.text))

      is SettingsEvent.OpenGestureList -> sendUiEvent(UiEvent.Navigate(SettingsScreen.GESTURE_SETTINGS_LIST + "/${event.gesture}"))
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