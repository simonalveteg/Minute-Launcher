package com.example.android.minutelauncher

import android.content.Context
import androidx.room.Room
import com.example.android.minutelauncher.data.LauncherDAO
import com.example.android.minutelauncher.data.LauncherDatabase
import com.example.android.minutelauncher.data.LauncherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext appContext: Context): LauncherDatabase {
    return Room.databaseBuilder(
      appContext,
      LauncherDatabase::class.java,
      "launcher-database"
    ).fallbackToDestructiveMigration().build()
  }

  @Provides
  @Singleton
  fun provideDao(database: LauncherDatabase): LauncherDAO {
    return database.launcherDao()
  }

  @Provides
  @Singleton
  fun provideRepository(dao: LauncherDAO): LauncherRepository {
    return LauncherRepository(dao)
  }

  @Provides
  fun provideContext(@ApplicationContext appContext: Context): Context {
    return appContext
  }
}