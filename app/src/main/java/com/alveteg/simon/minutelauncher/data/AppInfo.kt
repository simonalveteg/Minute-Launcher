package com.alveteg.simon.minutelauncher.data

data class AppInfo(
  val app: App,
  val favorite: Boolean,
  val mindfulDelay: Int,
  val usage: List<UsageStatistics>
) {

  fun getTitle() = this.app.displayTitle ?: this.app.appTitle

  companion object {
    val EMPTY = AppInfo(App.EMPTY, false, 0, emptyList())

    fun placeholder(id: Int) = EMPTY.copy(
      app = App(packageName = "placeholder_$id", appTitle = "")
    )
  }
}
