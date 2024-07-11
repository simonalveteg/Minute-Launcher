package com.alveteg.simon.minutelauncher.data

import androidx.room.Embedded

data class AppInfo(
  @Embedded val app: App,
  val favorite: Boolean,
  val usage: List<UsageStatistics>,
)

data class FavoriteAppInfo(
  val favoriteApp: FavoriteAppWithApp,
  val usage: List<UsageStatistics>,
)

fun FavoriteAppInfo.toAppInfo(): AppInfo {
  return AppInfo(
    app = this.favoriteApp.app,
    favorite = true,
    usage = this.usage,
  )
}
