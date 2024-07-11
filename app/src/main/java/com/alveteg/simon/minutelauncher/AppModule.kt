package com.alveteg.simon.minutelauncher

import android.content.Context
import androidx.room.Room
import com.alveteg.simon.minutelauncher.data.AccessTimerMappingCallback
import com.alveteg.simon.minutelauncher.data.AccessTimerMappingDao
import com.alveteg.simon.minutelauncher.data.ApplicationRepository
import com.alveteg.simon.minutelauncher.data.LauncherDao
import com.alveteg.simon.minutelauncher.data.LauncherDatabase
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideDatabase(
    @ApplicationContext appContext: Context,
    accessTimerMappingProvider: Provider<AccessTimerMappingDao>
  ): LauncherDatabase {
    return Room.databaseBuilder(
      appContext,
      LauncherDatabase::class.java,
      "launcher-database"
    )
      .fallbackToDestructiveMigration()
      .addCallback(AccessTimerMappingCallback(accessTimerMappingProvider))
      .build()
  }

  @Provides
  @Singleton
  fun provideLauncherDao(database: LauncherDatabase): LauncherDao {
    return database.launcherDao()
  }

  @Provides
  @Singleton
  fun provideAccessTimerMappingDao(database: LauncherDatabase): AccessTimerMappingDao {
    return database.accessTimerMappingDao()
  }

  @Provides
  @Singleton
  fun provideRepository(
    dao: LauncherDao,
    accessTimerMappingDao: AccessTimerMappingDao,
  ): LauncherRepository {
    return LauncherRepository(dao, accessTimerMappingDao)
  }

  @Provides
  @Singleton
  fun provideApplicationRepository(context: Context): ApplicationRepository {
    return ApplicationRepository(context)
  }

  @Provides
  fun provideContext(@ApplicationContext appContext: Context): Context {
    return appContext
  }
}