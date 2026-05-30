package com.alveteg.simon.minutelauncher.data

data class AppInfo(
  val app: App,
  val favorite: Boolean,
  val timer: Int,
  val usage: List<UsageStatistics>
) {
  companion object {
    val EMPTY = AppInfo(App.EMPTY, false, 0, emptyList())

    fun placeholder(id: Int) = EMPTY.copy(
      app = App(packageName = "placeholder_$id", appTitle = "")
    )
  }
}

data class FavoriteAppInfo(
  val favoriteApp: FavoriteAppWithApp,
  val appInfo: AppInfo,
)

