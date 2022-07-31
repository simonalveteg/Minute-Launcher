package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    application: Application,
) : ViewModel() {
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private val currentTime = System.currentTimeMillis()
    private val startTime =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    private val usageStatsManager = application.applicationContext.getSystemService(
        ComponentActivity.USAGE_STATS_SERVICE
    ) as UsageStatsManager
    private val appList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        currentTime
    )
    private val mainIntent = Intent().apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val searchTerm = mutableStateOf("")

    private val pm = application.applicationContext.packageManager
    private var installedPackages = pm.queryIntentActivities(mainIntent, 0).sortedBy {
        it.loadLabel(pm).toString().lowercase()
    }
    var applicationList by mutableStateOf(installedPackages)
        private set

    fun getUsageForApp(packageName: String) =
        mutableStateOf(appList.find { it.packageName == packageName }?.totalTimeInForeground ?: 0)

    fun getAppTitle(app: ResolveInfo) = mutableStateOf(app.loadLabel(pm).toString())

    fun getTotalUsage() = mutableStateOf(appList.sumOf { it.totalTimeInForeground })

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun updateSearch(text: String) {
        searchTerm.value = text
        applicationList = installedPackages.filter {
            it.loadLabel(pm).toString()
                .replace(" ", "")
                .replace("-", "")
                .contains(searchTerm.value, true)
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OpenApplication -> {
                Log.d("VIEWMODEL","Open application")
                sendUiEvent(UiEvent.ShowToast(getAppTitle(event.app).value))
                pm.getLaunchIntentForPackage(event.app.activityInfo.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }?.let {
                    sendUiEvent(UiEvent.StartActivity(it))
                }
                updateSearch("")
            }
            is Event.UpdateSearch -> {
                Log.d("VIEWMODEL","Update search")
                updateSearch(event.searchTerm)
            }
            is Event.CloseAppsList -> {
                Log.d("VIEWMODEL","Close apps list")
                updateSearch("")
                sendUiEvent(UiEvent.HideAppsList)
            }
        }
    }
}