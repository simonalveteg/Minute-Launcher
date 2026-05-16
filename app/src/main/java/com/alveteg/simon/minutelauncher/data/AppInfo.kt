package com.alveteg.simon.minutelauncher.data

data class AppInfo(
  val app: App,
  val favorite: Boolean,
  val timer: Int,
  val usage: List<UsageStatistics>,
)

data class FavoriteAppInfo(
  val favoriteApp: FavoriteAppWithApp,
  val appInfo: AppInfo,
)

