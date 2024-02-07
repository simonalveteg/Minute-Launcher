package com.alveteg.simon.minutelauncher.data

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UsageRepository @Inject constructor(@ApplicationContext private val context: Context) {
  private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

  fun queryUsageStats(): Flow<Map<String, Long>> = flow {
    while (currentCoroutineContext().isActive) {
      val currentTime = Calendar.getInstance().timeInMillis
      val startTime = Calendar.getInstance()
        .apply {
          timeZone = TimeZone.getDefault()
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
        }.timeInMillis
      Timber.d("Time since midnight: ${currentTime.minus(startTime)}")
      val usageStats = usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime)
        .filter { context.packageName != it.key }
        .mapValues { it.value.totalTimeInForeground }
      emit(usageStats)
      delay(TimeUnit.MINUTES.toMillis(1))
    }
  }
}