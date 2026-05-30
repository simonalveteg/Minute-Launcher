package com.alveteg.simon.minutelauncher.data

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
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  if (this == null || this < 1.seconds) {
    val zeroString = if (expanded) "0 minutes" else "0m"
    return if (!blankIfZero) zeroString else ""
  }

  val hours = inWholeHours
  val minutes = inWholeMinutes % 60
  val seconds = inWholeSeconds % 60

  val sb = StringBuilder()

  if (hours != 0L) {
    sb.append("${hours}h ")
  }

  if (minutes != 0L) {
    val suffix = if (expanded) (if (minutes == 1L) " minute" else " minutes") else "m"
    sb.append("${minutes}$suffix")
  }

  if (hours == 0L && minutes == 0L && seconds != 0L) {
    val suffix = if (expanded) (if (seconds == 1L) " second" else " seconds") else "s"
    sb.append("${seconds}$suffix")
  }

  return sb.toString().trim()
}

fun List<UsageStatistics>.toTimeUsed(
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  return this.sumOf { it.usageDuration }.toTimeUsed(blankIfZero, expanded)
}