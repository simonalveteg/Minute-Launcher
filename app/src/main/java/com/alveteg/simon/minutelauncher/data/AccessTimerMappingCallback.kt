package com.alveteg.simon.minutelauncher.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class AccessTimerMappingCallback @Inject constructor(
  private val daoProvider: Provider<AccessTimerMappingDao>
) : RoomDatabase.Callback() {

  private val applicationScope = CoroutineScope(SupervisorJob())

  override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)
    applicationScope.launch(Dispatchers.IO) {
      populateDatabase()
    }
  }

  private fun populateDatabase() {
    Timber.d("Populating database")
    val dao = daoProvider.get()
    val defaultMappings = listOf(
      AccessTimerMapping(AccessTimer.DEFAULT, 5),
      AccessTimerMapping(AccessTimer.NONE, 0),
      AccessTimerMapping(AccessTimer.SHORT, 2),
      AccessTimerMapping(AccessTimer.MEDIUM, 5),
      AccessTimerMapping(AccessTimer.LONG, 10),
      AccessTimerMapping(AccessTimer.ETERNITY, 15),
    )
    defaultMappings.forEach {
      Timber.d("For ${it.enum.name}")
      if (!dao.hasMapping(it.enum)) {
        dao.insertMapping(it)
        Timber.d("Inserting value ${it.integerValue}")
      }
    }
  }
}