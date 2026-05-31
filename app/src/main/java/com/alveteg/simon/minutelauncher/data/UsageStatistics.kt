package com.alveteg.simon.minutelauncher.data

import android.content.Context
import com.alveteg.simon.minutelauncher.R
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class UsageStatistics(
  val packageName: String,
  val usageDate: LocalDate,
  val usageDuration: Duration
)

/**
 * Sums [Duration] values provided by the [selector] function.
 */
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Duration?): Duration {
  return this.fold(Duration.ZERO) { acc, element ->
    acc + (selector(element) ?: Duration.ZERO)
  }
}

fun Duration?.toTimeUsed(
  context: Context? = null,
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  if (this == null || this < 1.seconds) {
    if (blankIfZero) return ""
    return if (expanded) {
      context?.resources?.getQuantityString(R.plurals.time_minutes, 0, 0) ?: "0 minutes"
    } else {
      context?.getString(R.string.time_m, 0) ?: "0m"
    }
  }

  val hours = inWholeHours
  val minutes = inWholeMinutes % 60
  val seconds = inWholeSeconds % 60

  val sb = StringBuilder()

  if (hours != 0L) {
    if (expanded) {
      sb.append(context?.resources?.getQuantityString(R.plurals.time_hours, hours.toInt(), hours) ?: "${hours}h")
    } else {
      sb.append(context?.getString(R.string.time_h, hours) ?: "${hours}h")
    }
    sb.append(" ")
  }

  if (minutes != 0L) {
    if (expanded) {
      sb.append(context?.resources?.getQuantityString(R.plurals.time_minutes, minutes.toInt(), minutes) ?: "${minutes}m")
    } else {
      sb.append(context?.getString(R.string.time_m, minutes) ?: "${minutes}m")
    }
  }

  if (hours == 0L && minutes == 0L && seconds != 0L) {
    if (expanded) {
      sb.append(context?.resources?.getQuantityString(R.plurals.time_seconds, seconds.toInt(), seconds) ?: "${seconds}s")
    } else {
      sb.append(context?.getString(R.string.time_s, seconds) ?: "${seconds}s")
    }
  }

  return sb.toString().trim()
}

fun List<UsageStatistics>.toTimeUsed(
  context: Context? = null,
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  return this.sumOf { it.usageDuration }.toTimeUsed(context, blankIfZero, expanded)
}
