package com.alveteg.simon.minutelauncher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessTimerMappingDao {
  @Query("SELECT * FROM AccessTimerMapping")
  fun getAllMappings(): Flow<List<AccessTimerMapping>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertMapping(mapping: AccessTimerMapping)

  @Query("DELETE FROM AccessTimerMapping")
  fun deleteAllMappings()

  @Query("SELECT * FROM AccessTimerMapping WHERE enum = :accessTimer")
  fun hasMapping(accessTimer: AccessTimer): Boolean
}
