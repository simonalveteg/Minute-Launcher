package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    application: Application,
) : ViewModel() {
    private val currentTime = System.currentTimeMillis()
    private val startTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 5) }.timeInMillis
    private val usageStatsManager = application.applicationContext.getSystemService(
        ComponentActivity.USAGE_STATS_SERVICE) as UsageStatsManager
    private val appList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        currentTime
    )

    init {
        logUsage()
    }

    fun getUsageForApp(packageName: String) = appList.find { it.packageName == packageName }?.totalTimeVisible ?: 0

    private fun logUsage() {
        appList.forEach {
            Log.d("APP_USAGE","${it.packageName} used for: ${it.totalTimeVisible}")
        }
    }
}