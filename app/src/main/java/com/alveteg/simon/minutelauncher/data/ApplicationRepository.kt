package com.alveteg.simon.minutelauncher.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ApplicationRepository @Inject constructor(
  @ApplicationContext private val context: Context
) {

  init {
    Timber.d("ApplicationRepository initialised")
  }

  private val packageManager = context.packageManager
  private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
  private var callback: LauncherApps.Callback? = null

  private val usageStatsManager =
    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

  private val _usageStats = MutableSharedFlow<List<UsageStatistics>>(replay = 1)
  val usageStats = _usageStats.asSharedFlow()


  fun registerCallback(callback: LauncherApps.Callback) {
    launcherApps.registerCallback(callback)
  }

  fun unregisterCallback() {
    callback?.let {
      launcherApps.unregisterCallback(it)
      callback = null
    }
  }

  private fun getLauncherApps(): Set<String> {
    val launcherIntent = Intent(Intent.ACTION_MAIN, null)
    launcherIntent.addCategory(Intent.CATEGORY_HOME)
    return packageManager.queryIntentActivities(launcherIntent, 0)
      .map { it.activityInfo.packageName }
      .toSet()
  }

  fun getApps(): List<App> {
    return launcherApps.getActivityList(null, launcherApps.profiles.firstOrNull())
      .filter { it.applicationInfo.packageName != context.packageName }
      .map { it.toApp() }
  }

  fun getLaunchIntentForPackage(packageName: String): Intent? {
    return packageManager.getLaunchIntentForPackage(packageName)?.apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }
  }

  suspend fun startUsageUpdater() {
    while (currentCoroutineContext().isActive) {
      val usageStats = getDailyStatsForWeek()

      _usageStats.emit(usageStats)
      delay(TimeUnit.MINUTES.toMillis(1))
    }
  }

  private fun getDailyStatsForWeek(): List<UsageStatistics> {
    val today = LocalDate.now()
    val dates = listOf(
      today,
      today.minusDays(1),
      today.minusDays(2),
      today.minusDays(3),
      today.minusDays(4),
      today.minusDays(5),
      today.minusDays(6)
    )

    val launcherApps = getLauncherApps()
    return dates.flatMap { date ->
      Timber.d("Getting stats for date: $date")
      getDailyStats(date)
        .filter { !launcherApps.contains(it.packageName) }
        .filter { context.packageName != it.packageName }
    }.also { Timber.d("--- ${it.size} packages, ${it.sumOf { it.usageDuration }.toTimeUsed()} ") }
  }

  /**
   * Returns the stats for the [date] (defaults to today)
   * https://stackoverflow.com/questions/36238481/android-usagestatsmanager-not-returning-correct-daily-results
   * https://github.com/MuntashirAkon/AppManager/blob/6491491f1aa685892ed8deecad4d685c78ac5f94/app/src/main/java/io/github/muntashirakon/AppManager/usage/AppUsageStatsManager.java#L203
   */
  private fun getDailyStats(date: LocalDate = LocalDate.now()): List<UsageStatistics> {
    // The timezones we'll need
    val utc = ZoneId.of("UTC")
    val defaultZone = ZoneId.systemDefault()

    // Set the starting and ending times to be midnight in UTC time
    val startDate = date.atStartOfDay(defaultZone).withZoneSameInstant(utc)
    val start = startDate.toInstant().toEpochMilli()
    val end = startDate.plusDays(1).toInstant().toEpochMilli()

    // This will keep a map of all of the events per package name
    val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()

    // Query the list of events that has happened within that time frame
    val systemEvents = usageStatsManager.queryEvents(start, end)
    while (systemEvents.hasNextEvent()) {
      val event = UsageEvents.Event()
      systemEvents.getNextEvent(event)

      // Get the list of events for the package name, create one if it doesn't exist
      val packageEvents = sortedEvents[event.packageName] ?: mutableListOf()
      packageEvents.add(event)
      sortedEvents[event.packageName] = packageEvents
    }

    val stats = mutableListOf<UsageStatistics>()

    sortedEvents.forEach { (packageName, events) ->
      var startTime = 0L
      var endTime = 0L
      var totalTime = 0L
      events.forEach {

        if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
          if (startTime == 0L) startTime = it.timeStamp
        } else if (it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
          // if activity paused without startTime, it must mean that the app was started before midnight
          if (startTime > 0L) endTime = it.timeStamp
        }

        // If both start and end are defined, we have a session
        if (startTime != 0L && endTime != 0L) {
          // Add the session time to the total time
          totalTime += endTime - startTime
          // Reset the start/end times to 0
          startTime = 0L
          endTime = 0L
        }
      }
      stats.add(
        UsageStatistics(
          packageName = packageName,
          usageDate = date,
          usageDuration = totalTime
        )
      )
    }
    return stats
  }
}

// Helper class to keep track of all of the stats
data class Stat(val packageName: String, val totalTime: Long)
