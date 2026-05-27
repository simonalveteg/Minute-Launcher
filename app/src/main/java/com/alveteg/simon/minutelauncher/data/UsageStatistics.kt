package com.alveteg.simon.minutelauncher.data

import java.time.LocalDate
import kotlin.time.Duration

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
  if (this == null || this == Duration.ZERO) {
    val zeroString = if (expanded) "0 minutes" else "0m"
    return if (!blankIfZero) zeroString else ""
  }

  val hours = inWholeHours
  val minutes = inWholeMinutes % 60

  if (inWholeMinutes == 0L && this > Duration.ZERO) {
    return if (expanded) "<1 minute" else "<1m"
  }

  val sb = StringBuilder()
  if (hours != 0L) {
    sb.append("${hours}h ")
  }

  if (minutes != 0L) {
    val suffix = if (expanded) (if (minutes == 1L) " minute" else " minutes") else "m"
    sb.append("${minutes}$suffix")
  }

  return sb.toString().trim()
}

fun List<UsageStatistics>.toTimeUsed(
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  return this.sumOf { it.usageDuration }.toTimeUsed(blankIfZero, expanded)
}