package com.alveteg.simon.minutelauncher.data

data class AppInfo(
  val app: App,
  val favorite: Boolean = false,
  val mindfulDelay: Int = PreferenceRepository.Defaults.MINDFUL_DELAY_LENGTH,
  val usage: List<UsageStatistics> = emptyList()
) {
  companion object {
    val EMPTY = AppInfo(App.EMPTY)

    fun placeholder(id: Int) = EMPTY.copy(
      app = App(packageName = "placeholder_$id", appTitle = "")
    )
  }
}
