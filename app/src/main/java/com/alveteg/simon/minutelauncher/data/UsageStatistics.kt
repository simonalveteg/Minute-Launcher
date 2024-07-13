package com.alveteg.simon.minutelauncher.data

import java.time.LocalDate

data class UsageStatistics(
  val packageName: String,
  val usageDate: LocalDate,
  val usageDuration: Long
)
