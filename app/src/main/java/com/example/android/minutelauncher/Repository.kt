package com.example.android.minutelauncher

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import java.util.*

class Repository(
    mContext: Context
) {
    private val currentTime = System.currentTimeMillis()
    private val startTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 5) }.timeInMillis
    private val usageStatsManager = mContext.getSystemService(ComponentActivity.USAGE_STATS_SERVICE) as UsageStatsManager
    private val appList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        currentTime
    )
    init {
        appList.forEach {
            Log.d("APP_USAGE","${it.packageName} used for: ${it.totalTimeInForeground}")
        }
    }
}