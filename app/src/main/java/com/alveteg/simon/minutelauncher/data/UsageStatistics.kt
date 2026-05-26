package com.alveteg.simon.minutelauncher.data

import java.time.LocalDate

data class UsageStatistics(
  val packageName: String,
  val usageDate: LocalDate,
  val usageDuration: Long
)

fun Long?.toTimeUsed(
  blankIfZero: Boolean = true,
  expanded: Boolean = false
): String {
  val zeroString = if (expanded) "0 minutes" else "0m"
  if (this == null || this == 0L) return if (!blankIfZero) zeroString else ""

  val minutes = div(60000)
  val hours = minutes.div(60)

  if (minutes == 0L && this > 0) return if (expanded) "<1 minute" else "<1m"

  val sb = StringBuilder()
  if (hours != 0L) {
    sb.append("${hours}h ")
  }

  val remainingMinutes = minutes % 60
  if (remainingMinutes != 0L) {
    val suffix = if (expanded) {
      if (remainingMinutes == 1L) " minute" else " minutes"
    } else {
      "m"
    }
    sb.append("${remainingMinutes}$suffix")
  }

  return sb.toString().trim()
}
