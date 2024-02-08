package com.alveteg.simon.minutelauncher.data

data class AppInfo(
  val app: App,
  val favorite: Boolean,
  val usage: Long = 0L,
  val timesOpened: Int = 0
)

data class FavoriteAppInfo(
  val favoriteApp: FavoriteAppWithApp,
  val usage: Long = 0L,
  val timesOpened: Int = 0
)

fun FavoriteAppInfo.toAppInfo(): AppInfo {
  return AppInfo(
    app = this.favoriteApp.app,
    favorite = true,
    usage = this.usage,
    timesOpened = this.timesOpened
  )
}
