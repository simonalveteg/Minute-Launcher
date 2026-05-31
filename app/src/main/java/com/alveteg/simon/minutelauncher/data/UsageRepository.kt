package com.alveteg.simon.minutelauncher.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.telecom.TelecomManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class UsageRepository @Inject constructor(
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

  private val _usageStats = MutableStateFlow<List<UsageStatistics>>(emptyList())
  val usageStats = _usageStats.asStateFlow()


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

  fun getAppSignals(): List<AppSignals> {
    val allApps = launcherApps.getActivityList(null, launcherApps.profiles.firstOrNull())

    // Default Browser
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
    val defaultBrowser = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
      ?.activityInfo?.packageName

    // Default SMS
    val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)

    // Default Dialer
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val defaultDialer = telecomManager.defaultDialerPackage

    // Apps that can handle share
    val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
    val shareHandlers = packageManager.queryIntentActivities(shareIntent, 0)
      .map { it.activityInfo.packageName }
      .toSet()

    return allApps.map { info ->
      val appInfo = info.applicationInfo
      val pkg = appInfo.packageName
      AppSignals(
        packageName = pkg,
        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
        isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0,
        isDefaultBrowser = pkg == defaultBrowser,
        isDefaultSms = pkg == defaultSms,
        isDefaultDialer = pkg == defaultDialer,
        canHandleShare = shareHandlers.contains(pkg)
      )
    }
  }

  suspend fun startUsageUpdater() {
    Timber.d("Starting usage updater.")
    while (currentCoroutineContext().isActive) {
      val stats = getDailyStatsForWeek()
      _usageStats.value = stats
      delay(TimeUnit.MINUTES.toMillis(1))
    }
  }

  private fun getDailyStatsForWeek(): List<UsageStatistics> {
    val today = LocalDate.now()
    val dates = mutableListOf<LocalDate>()
    repeat(7) { dates += today.minusDays(it.toLong()) }

    val launcherApps = getLauncherApps()
    return dates.flatMap { date ->
      getDailyStats(date)
        .filter { !launcherApps.contains(it.packageName) }
        .filter { context.packageName != it.packageName }
        .also { statsList ->
          val totalDuration = statsList.sumOf { it.usageDuration }
          Timber.d("$date: ${statsList.size} packages, ${totalDuration.toTimeUsed()}")
        }
    }
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
          usageDuration = totalTime.milliseconds
        )
      )
    }
    return stats
  }
}

data class AppSignals(
  val packageName: String,
  val isSystemApp: Boolean,
  val isUpdatedSystemApp: Boolean,
  val isDefaultBrowser: Boolean,
  val isDefaultSms: Boolean,
  val isDefaultDialer: Boolean,
  val canHandleShare: Boolean
)
